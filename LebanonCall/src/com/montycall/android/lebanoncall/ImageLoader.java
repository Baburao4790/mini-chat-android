package com.montycall.android.lebanoncall;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.widget.ImageView;

public class ImageLoader {
    
    MemoryCache memoryCache=new MemoryCache();
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService; 
    private Context mContext;
    
    public ImageLoader(Context context){
        executorService=Executors.newFixedThreadPool(5);
        mContext = context;
    }
    
    final int stub_id=R.drawable.person_icon;
    public void DisplayImage(byte[] array , ImageView imageView, String key, String id, int isAndroid)
    {
        imageViews.put(imageView, key);
        Bitmap bitmap=memoryCache.get(key);
        if(bitmap!=null)
            imageView.setImageBitmap(bitmap);
        else
        {
            queuePhoto(array, imageView, key, id, isAndroid);
            imageView.setImageResource(stub_id);
        }
    }
        
    private void queuePhoto(byte[] array, ImageView imageView, String key, String id, int isAndroid)
    {
        PhotoToLoad p=new PhotoToLoad(array, imageView, key, id, isAndroid);
        executorService.submit(new PhotosLoader(p));
    }
    
    
    
    private Bitmap getBitmap(byte[] array, String id, int isAndroid) 
    {
       
        try {
        	if(array != null) {
	        	BitmapFactory.Options o = new BitmapFactory.Options();
	            o.inJustDecodeBounds = true;
	            BitmapFactory.decodeByteArray(array, 0, array.length,o);
	            //Find the correct scale value. It should be the power of 2.
	            final int REQUIRED_SIZE=70;
	            int width_tmp=o.outWidth, height_tmp=o.outHeight;
	            int scale=1;
	            while(true){
	                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
	                    break;
	                width_tmp/=2;
	                height_tmp/=2;
	                scale*=2;
	            }
	            Bitmap bitmap=null;
	            BitmapFactory.Options opts=new BitmapFactory.Options();
	            opts.inSampleSize=scale;
	            opts.inDither=false;                     //Disable Dithering mode
				opts.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
				opts.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
				opts.inTempStorage=new byte[8 * 1024];
				bitmap = BitmapFactory.decodeByteArray(array, 0, array.length,opts);
				bitmap = BitmapFactory.decodeByteArray(array, 0, array.length,opts);
				
				
	            return bitmap;
        	}else {
        		Bitmap bitmap=null;
        		if((id != null) && (!id.equalsIgnoreCase(""))) {
 				    if(Build.VERSION.SDK_INT < 11) {
				    	bitmap = loadContactPhoto(mContext.getContentResolver(), id);
				    } else {
						try {
							bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(Uri.parse(id)));
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				    }			    
        		}
				return bitmap;
			}
        } catch (Throwable ex){
           ex.printStackTrace();
           if(ex instanceof OutOfMemoryError)
               memoryCache.clear();
           return null;
        }
       
    }
    
    private static Bitmap loadContactPhoto(ContentResolver cr, String photo_id) {
        Long id = Long.parseLong(photo_id);
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input == null) {
        	//Log.e(LOG_TAG, "Returning NULL as the bitmap");
            return null;
        }
        return BitmapFactory.decodeStream(input);
    } 

        
    //Task for the queue
    private class PhotoToLoad
    {
        public String key;
        public ImageView imageView;
        public byte[] array;
        public int isAndroid;
        public String id;
        public PhotoToLoad(byte[] a, ImageView i, String u, String p, int q){
            key=u; 
            imageView=i;
            array = a;
            id = p;
            isAndroid = q;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        @Override
        public void run() {
            try{
                if(imageViewReused(photoToLoad))
                    return;
                Bitmap bmp=getBitmap(photoToLoad.array, photoToLoad.id, photoToLoad.isAndroid);
                if(bmp != null) {
                	memoryCache.put(photoToLoad.key, bmp);
                	if(imageViewReused(photoToLoad))
                        return;
                    BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
                    Activity a=(Activity)photoToLoad.imageView.getContext();
                    a.runOnUiThread(bd);
                }
            }catch(Throwable th){
                th.printStackTrace();
            }
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.key))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }

    public void clearCache() {
        memoryCache.clear();
    }

}

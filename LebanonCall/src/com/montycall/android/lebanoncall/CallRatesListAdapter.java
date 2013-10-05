package com.montycall.android.lebanoncall;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.montycall.android.lebanoncall.CallRatesActivity.CallRates;

public class CallRatesListAdapter extends ArrayAdapter<CallRates> {

	private static final String TAG = "CallRatesListAdapter";

	private ArrayList<CallRates> mList;
	private static Context mContext = null;
	private CallRates objItem;

	public CallRatesListAdapter(Context context, ArrayList<CallRates> callRates) {
		super(context, 0, callRates);
		mContext = context;
		mList = callRates;
	}

	ViewHolder holder;

	public static class ViewHolder {
		TextView name;
		TextView price;
		ImageView flag;
	}

	public static class ViewHolderSection {
		public TextView section;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		objItem = (CallRates) mList.get(position);
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = (View) LayoutInflater.from(mContext).inflate(
					R.layout.callratelist_item_layout, null, false);
			holder.name = (TextView) convertView.findViewById(R.id.country);
			holder.price = (TextView) convertView.findViewById(R.id.cost);
			holder.flag = (ImageView) convertView
					.findViewById(R.id.country_flag);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(objItem.countryName);
		holder.price.setText(objItem.prices);
		setCountryFlag(holder, objItem.countryName, position);		
		return convertView;
	}

	private void setCountryFlag(ViewHolder holder, String countryName,
			int position) {

		if (countryName.contains("Afghanistan")) {
			holder.flag.setImageResource(R.drawable.af);
		} else if (countryName.contains("Albania")) {
			holder.flag.setImageResource(R.drawable.al);
		} else if (countryName.contains("Algeria")) {
			holder.flag.setImageResource(R.drawable.dz);
		} else if (countryName.contains("American Samoa")) {
			holder.flag.setImageResource(R.drawable.as);
		} else if (countryName.contains("Andorra")) {
			holder.flag.setImageResource(R.drawable.ad);
		} else if (countryName.contains("Angola")) {
			holder.flag.setImageResource(R.drawable.ao);
		} else if (countryName.contains("Anguilla")) {
			holder.flag.setImageResource(R.drawable.ai);
		} else if (countryName.contains("Antigua")) {
			holder.flag.setImageResource(R.drawable.ag);
		} else if (countryName.contains("Argentina")) {
			holder.flag.setImageResource(R.drawable.ar);
		} else if (countryName.contains("Armenia")) {
			holder.flag.setImageResource(R.drawable.am);
		} else if (countryName.contains("Armenia")) {
			holder.flag.setImageResource(R.drawable.am);
		} else if (countryName.contains("Aruba")) {
			holder.flag.setImageResource(R.drawable.aw);
		} else if (countryName.contains("Australia")) {
			holder.flag.setImageResource(R.drawable.au);
		} else if (countryName.contains("Austria")) {
			holder.flag.setImageResource(R.drawable.at);
		} else if (countryName.contains("Azerbaijan")) {
			holder.flag.setImageResource(R.drawable.az);
		} else if (countryName.contains("Bahamas")) {
			holder.flag.setImageResource(R.drawable.bs);
		} else if (countryName.contains("Bahrain")) {
			holder.flag.setImageResource(R.drawable.bh);
		} else if (countryName.contains("Banglagesh")) {
			holder.flag.setImageResource(R.drawable.bd);
		} else if (countryName.contains("Barbados")) {
			holder.flag.setImageResource(R.drawable.bb);
		} else if (countryName.contains("Belarus")) {
			holder.flag.setImageResource(R.drawable.by);
		} else if (countryName.contains("Belgium")) {
			holder.flag.setImageResource(R.drawable.be);
		} else if (countryName.contains("Belize")) {
			holder.flag.setImageResource(R.drawable.bl);
		} else if (countryName.contains("Benin")) {
			holder.flag.setImageResource(R.drawable.bj);
		} else if (countryName.contains("Bermuda")) {
			holder.flag.setImageResource(R.drawable.bm);
		} else if (countryName.contains("Butan")) {
			holder.flag.setImageResource(R.drawable.bt);
		} else if (countryName.contains("Bolivia")) {
			holder.flag.setImageResource(R.drawable.bo);
		} else if (countryName.contains("Bosnia")) {
			holder.flag.setImageResource(R.drawable.ba);
		} else if (countryName.contains("Botswana")) {
			holder.flag.setImageResource(R.drawable.bw);
		} else if (countryName.contains("Brazil")) {
			holder.flag.setImageResource(R.drawable.br);
		} else if (countryName.contains("British Virgin Islands")) {
			holder.flag
					.setImageResource(R.drawable.british_antarctic_territory);
		} else if (countryName.contains("Brunei")) {
			holder.flag.setImageResource(R.drawable.bn);
		} else if (countryName.contains("Bulgaria")) {
			holder.flag.setImageResource(R.drawable.bg);
		} else if (countryName.contains("Burkina Faso")) {
			holder.flag.setImageResource(R.drawable.bf);
		} else if (countryName.contains("Burundi")) {
			holder.flag.setImageResource(R.drawable.bi);
		} else if (countryName.contains("Cambodia")) {
			holder.flag.setImageResource(R.drawable.kh);
		} else if (countryName.contains("Cameroon")) {
			holder.flag.setImageResource(R.drawable.cm);
		} else if (countryName.contains("Canada")) {
			holder.flag.setImageResource(R.drawable.ca);
		} else if (countryName.contains("Cape Verde")) {
			holder.flag.setImageResource(R.drawable.cv);
		} else if (countryName.contains("Cayman Islands")) {
			holder.flag.setImageResource(R.drawable.ky);
		} else if (countryName.contains("Central African Republic")) {
			holder.flag.setImageResource(R.drawable.cf);
		} else if (countryName.contains("Chad -")) {
			holder.flag.setImageResource(R.drawable.ro);
		} else if (countryName.contains("Chile -")) {
			holder.flag.setImageResource(R.drawable.cl);
		} else if (countryName.contains("China -")) {
			holder.flag.setImageResource(R.drawable.cn);
		} else if (countryName.contains("Colombia")) {
			holder.flag.setImageResource(R.drawable.co);
		} else if (countryName.contains("Comoros")) {
			holder.flag.setImageResource(R.drawable.km);
		} else if (countryName.contains("Congo Brazzaville")) {
			holder.flag.setImageResource(R.drawable.cd);
		} else if (countryName.contains("Cook -")) {
			holder.flag.setImageResource(R.drawable.ck);
		} else if (countryName.contains("Costa Rica")) {
			holder.flag.setImageResource(R.drawable.cr);
		} else if (countryName.contains("Croatia")) {
			holder.flag.setImageResource(R.drawable.hr);
		} else if (countryName.contains("Cuba -")) {
			holder.flag.setImageResource(R.drawable.cu);
		} else if (countryName.contains("Cyprus")) {
			holder.flag.setImageResource(R.drawable.cy);
		} else if (countryName.contains("Czech Republic")) {
			holder.flag.setImageResource(R.drawable.cz);
		} else if (countryName.contains("Denmark")) {
			holder.flag.setImageResource(R.drawable.dk);
		} else if (countryName.contains("Djibouti")) {
			holder.flag.setImageResource(R.drawable.dj);
		} else if (countryName.contains("Dominica -")) {
			holder.flag.setImageResource(R.drawable.dm);
		} else if (countryName.contains("Dominican Republic")) {
			holder.flag.setImageResource(R.drawable.doo);
		} else if (countryName.contains("DR of Congo")) {
			holder.flag.setImageResource(R.drawable.cd);
		} else if (countryName.contains("Ecuador")) {
			holder.flag.setImageResource(R.drawable.ec);
		} else if (countryName.contains("Egypt")) {
			holder.flag.setImageResource(R.drawable.eg);
		} else if (countryName.contains("El Salvador")) {
			holder.flag.setImageResource(R.drawable.ni);
		} else if (countryName.contains("Equatorial Guinea")) {
			holder.flag.setImageResource(R.drawable.gg);
		} else if (countryName.contains("Eritrea")) {
			holder.flag.setImageResource(R.drawable.er);
		} else if (countryName.contains("Estonia")) {
			holder.flag.setImageResource(R.drawable.ee);
		} else if (countryName.contains("Ethiopia")) {
			holder.flag.setImageResource(R.drawable.et);
		} else if (countryName.contains("Faeroe")) {
			holder.flag.setImageResource(R.drawable.fo);
		} else if (countryName.contains("Fiji")) {
			holder.flag.setImageResource(R.drawable.fj);
		} else if (countryName.contains("Finland")) {
			holder.flag.setImageResource(R.drawable.fi);
		} else if (countryName.contains("France")) {
			holder.flag.setImageResource(R.drawable.fr);
		} else if (countryName.contains("French")) {
			holder.flag.setImageResource(R.drawable.fr);
		} else if (countryName.contains("French")) {
			holder.flag.setImageResource(R.drawable.fr);
		} else if (countryName.contains("Gabon -")) {
			holder.flag.setImageResource(R.drawable.ga);
		} else if (countryName.contains("Gambia")) {
			holder.flag.setImageResource(R.drawable.gm);
		} else if (countryName.contains("Georgia")) {
			holder.flag.setImageResource(R.drawable.ge);
		} else if (countryName.contains("Germany")) {
			holder.flag.setImageResource(R.drawable.de);
		} else if (countryName.contains("Ghana -")) {
			holder.flag.setImageResource(R.drawable.gh);
		} else if (countryName.contains("Gibraltar")) {
			holder.flag.setImageResource(R.drawable.gi);
		} else if (countryName.contains("Greece")) {
			holder.flag.setImageResource(R.drawable.gr);
		} else if (countryName.contains("Greenland")) {
			holder.flag.setImageResource(R.drawable.gl);
		} else if (countryName.contains("Grenada")) {
			holder.flag.setImageResource(R.drawable.gd);
		} else if (countryName.contains("Guadeloupe")) {
			holder.flag.setImageResource(R.drawable.fr);
		} else if (countryName.contains("Guam")) {
			holder.flag.setImageResource(R.drawable.gu);
		} else if (countryName.contains("Guatemala")) {
			holder.flag.setImageResource(R.drawable.gt);
		} else if (countryName.contains("Guinea Bissau")) {
			holder.flag.setImageResource(R.drawable.gw);
		} else if (countryName.contains("Guinea Republic")) {
			holder.flag.setImageResource(R.drawable.gn);
		} else if (countryName.contains("Guyana")) {
			holder.flag.setImageResource(R.drawable.gy);
		} else if (countryName.contains("Haiti")) {
			holder.flag.setImageResource(R.drawable.ht);
		} else if (countryName.contains("Honduras")) {
			holder.flag.setImageResource(R.drawable.hn);
		} else if (countryName.contains("Hong Kong")) {
			holder.flag.setImageResource(R.drawable.hk);
		} else if (countryName.contains("Hungary")) {
			holder.flag.setImageResource(R.drawable.hu);
		} else if (countryName.contains("Iceland")) {
			holder.flag.setImageResource(R.drawable.is);
		} else if (countryName.contains("India")) {
			holder.flag.setImageResource(R.drawable.in);
		} else if (countryName.contains("Indonesia")) {
			holder.flag.setImageResource(R.drawable.id);
		} else if (countryName.contains("Iran")) {
			holder.flag.setImageResource(R.drawable.ir);
		} else if (countryName.contains("Iraq")) {
			holder.flag.setImageResource(R.drawable.iq);
		} else if (countryName.contains("Ireland")) {
			holder.flag.setImageResource(R.drawable.ie);
		} else if (countryName.contains("Israel")) {
			holder.flag.setImageResource(R.drawable.il);
		} else if (countryName.contains("Italy")) {
			holder.flag.setImageResource(R.drawable.it);
		} else if (countryName.contains("Ivory Coast")) {
			holder.flag.setImageResource(R.drawable.ivory);
		} else if (countryName.contains("Jamaica")) {
			holder.flag.setImageResource(R.drawable.jm);
		} else if (countryName.contains("Japan")) {
			holder.flag.setImageResource(R.drawable.jp);
		} else if (countryName.contains("Jordan")) {
			holder.flag.setImageResource(R.drawable.jo);
		} else if (countryName.contains("Kazakhstan")) {
			holder.flag.setImageResource(R.drawable.kz);
		} else if (countryName.contains("Kenya")) {
			holder.flag.setImageResource(R.drawable.ke);
		} else if (countryName.contains("Kiribati")) {
			holder.flag.setImageResource(R.drawable.ki);
		} else if (countryName.contains("Kuwait")) {
			holder.flag.setImageResource(R.drawable.kw);
		} else if (countryName.contains("Kyrghyzstan")) {
			holder.flag.setImageResource(R.drawable.kg);
		} else if (countryName.contains("Laos -")) {
			holder.flag.setImageResource(R.drawable.la);
		} else if (countryName.contains("Latvia")) {
			holder.flag.setImageResource(R.drawable.lv);
		} else if (countryName.contains("Lebanon")) {
			holder.flag.setImageResource(R.drawable.lb);
		} else if (countryName.contains("Lesotho")) {
			holder.flag.setImageResource(R.drawable.ls);
		} else if (countryName.contains("Liberia")) {
			holder.flag.setImageResource(R.drawable.lr);
		} else if (countryName.contains("Libya")) {
			holder.flag.setImageResource(R.drawable.ly);
		} else if (countryName.contains("Liechtenstein")) {
			holder.flag.setImageResource(R.drawable.li);
		} else if (countryName.contains("Lithuania")) {
			holder.flag.setImageResource(R.drawable.lt);
		} else if (countryName.contains("Luxemburg")) {
			holder.flag.setImageResource(R.drawable.lu);
		} else if (countryName.contains("Macau")) {
			holder.flag.setImageResource(R.drawable.mo);
		} else if (countryName.contains("Macedonia")) {
			holder.flag.setImageResource(R.drawable.mk);
		} else if (countryName.contains("Madagascara")) {
			holder.flag.setImageResource(R.drawable.mg);
		} else if (countryName.contains("Malawi")) {
			holder.flag.setImageResource(R.drawable.mw);
		} else if (countryName.contains("Malaysia")) {
			holder.flag.setImageResource(R.drawable.my);
		} else if (countryName.contains("Maldives")) {
			holder.flag.setImageResource(R.drawable.mv);
		} else if (countryName.contains("Mali -")) {
			holder.flag.setImageResource(R.drawable.ml);
		} else if (countryName.contains("Malta")) {
			holder.flag.setImageResource(R.drawable.mt);
		} else if (countryName.contains("Marshall")) {
			holder.flag.setImageResource(R.drawable.mh);
		} else if (countryName.contains("Martinique")) {
			holder.flag.setImageResource(R.drawable.mw);
		} else if (countryName.contains("Mauritania")) {
			holder.flag.setImageResource(R.drawable.mr);
		} else if (countryName.contains("Mauritius")) {
			holder.flag.setImageResource(R.drawable.mu);
		} else if (countryName.contains("Mayotte")) {
			holder.flag.setImageResource(R.drawable.fr);
		} else if (countryName.contains("Mexico")) {
			holder.flag.setImageResource(R.drawable.mx);
		} else if (countryName.contains("Micronesia")) {
			holder.flag.setImageResource(R.drawable.fm);
		} else if (countryName.contains("Moldova")) {
			holder.flag.setImageResource(R.drawable.md);
		} else if (countryName.contains("Monaco")) {
			holder.flag.setImageResource(R.drawable.mc);
		} else if (countryName.contains("Mongolia")) {
			holder.flag.setImageResource(R.drawable.mn);
		} else if (countryName.contains("Montenegro")) {
			holder.flag.setImageResource(R.drawable.me);
		} else if (countryName.contains("Morocco")) {
			holder.flag.setImageResource(R.drawable.ma);
		} else if (countryName.contains("Mozambique")) {
			holder.flag.setImageResource(R.drawable.mz);
		} else if (countryName.contains("Myanmar")) {
			holder.flag.setImageResource(R.drawable.mm);
		} else if (countryName.contains("Namibia")) {
			holder.flag.setImageResource(R.drawable.na);
		} else if (countryName.contains("Nauru")) {
			holder.flag.setImageResource(R.drawable.nr);
		} else if (countryName.contains("Nepal")) {
			holder.flag.setImageResource(R.drawable.np);
		} else if (countryName.contains("Netherlands -")) {
			holder.flag.setImageResource(R.drawable.nl);
		} else if (countryName.contains("Netherlands Antillas")) {
			holder.flag.setImageResource(R.drawable.an);
		} else if (countryName.contains("New Caledonia")) {
			holder.flag.setImageResource(R.drawable.fr);
		} else if (countryName.contains("New Zealand")) {
			holder.flag.setImageResource(R.drawable.nz);
		} else if (countryName.contains("Nicaragua")) {
			holder.flag.setImageResource(R.drawable.ni);
		} else if (countryName.contains("Niger")) {
			holder.flag.setImageResource(R.drawable.ne);
		} else if (countryName.contains("Nigeria")) {
			holder.flag.setImageResource(R.drawable.ng);
		} else if (countryName.contains("Niue")) {
			holder.flag.setImageResource(R.drawable.nu);
		} else if (countryName.contains("Norfolk")) {
			holder.flag.setImageResource(R.drawable.nf);
		} else if (countryName.contains("North Korea")) {
			holder.flag.setImageResource(R.drawable.kp);
		} else if (countryName.contains("Norway")) {
			holder.flag.setImageResource(R.drawable.no);
		} else if (countryName.contains("Oman")) {
			holder.flag.setImageResource(R.drawable.om);
		} else if (countryName.contains("Pakistan")) {
			holder.flag.setImageResource(R.drawable.pk);
		} else if (countryName.contains("Palau")) {
			holder.flag.setImageResource(R.drawable.pw);
		} else if (countryName.contains("Palestine")) {
			holder.flag.setImageResource(R.drawable.ps);
		} else if (countryName.contains("Panama")) {
			holder.flag.setImageResource(R.drawable.pa);
		} else if (countryName.contains("Papua New Guinea")) {
			holder.flag.setImageResource(R.drawable.pg);
		} else if (countryName.contains("Paraguay")) {
			holder.flag.setImageResource(R.drawable.py);
		} else if (countryName.contains("Peru -")) {
			holder.flag.setImageResource(R.drawable.pe);
		} else if (countryName.contains("Philippines")) {
			holder.flag.setImageResource(R.drawable.ph);
		} else if (countryName.contains("Poland")) {
			holder.flag.setImageResource(R.drawable.pl);
		} else if (countryName.contains("Portugal")) {
			holder.flag.setImageResource(R.drawable.pt);
		} else if (countryName.contains("Puerto Rico")) {
			holder.flag.setImageResource(R.drawable.pr);
		} else if (countryName.contains("Qatar")) {
			holder.flag.setImageResource(R.drawable.qa);
		} else if (countryName.contains("Reunion")) {
			holder.flag.setImageResource(R.drawable.fr);
		} else if (countryName.contains("Romania")) {
			holder.flag.setImageResource(R.drawable.ro);
		} else if (countryName.contains("Russia")) {
			holder.flag.setImageResource(R.drawable.ru);
		} else if (countryName.contains("Rwanda")) {
			holder.flag.setImageResource(R.drawable.rw);
		} else if (countryName.contains("Saint Kitts and Nevis")) {
			holder.flag.setImageResource(R.drawable.kn);
		} else if (countryName.contains("Saint Lucia")) {
			holder.flag.setImageResource(R.drawable.lc);
		} else if (countryName.contains("Saint Vincent and the Grenadines")) {
			holder.flag.setImageResource(R.drawable.vc);
		} else if (countryName.contains("Saipan")) {
			holder.flag.setImageResource(R.drawable.mp);
		} else if (countryName.contains("San Marino")) {
			holder.flag.setImageResource(R.drawable.sm);
		} else if (countryName.contains("Saudi Arabia")) {
			holder.flag.setImageResource(R.drawable.sa);
		} else if (countryName.contains("Senegal")) {
			holder.flag.setImageResource(R.drawable.sn);
		} else if (countryName.contains("Serbia")) {
			holder.flag.setImageResource(R.drawable.rs);
		} else if (countryName.contains("Seychelles")) {
			holder.flag.setImageResource(R.drawable.sc);
		} else if (countryName.contains("Sierra Leone")) {
			holder.flag.setImageResource(R.drawable.sl);
		} else if (countryName.contains("Singapore")) {
			holder.flag.setImageResource(R.drawable.sg);
		} else if (countryName.contains("Sint Maarten")) {
			holder.flag.setImageResource(R.drawable.ph);
		} else if (countryName.contains("Slovakia")) {
			holder.flag.setImageResource(R.drawable.sk);
		} else if (countryName.contains("Slovenia")) {
			holder.flag.setImageResource(R.drawable.si);
		} else if (countryName.contains("Somalia")) {
			holder.flag.setImageResource(R.drawable.so);
		} else if (countryName.contains("South Africa")) {
			holder.flag.setImageResource(R.drawable.za);
		} else if (countryName.contains("South Korea")) {
			holder.flag.setImageResource(R.drawable.kr);
		} else if (countryName.contains("South Sudan")) {
			holder.flag.setImageResource(R.drawable.sd);
		} else if (countryName.contains("Spain")) {
			holder.flag.setImageResource(R.drawable.es);
		} else if (countryName.contains("Sri Lanka")) {
			holder.flag.setImageResource(R.drawable.lk);
		} else if (countryName.contains("St Pierre & Miquelon")) {
			holder.flag.setImageResource(R.drawable.lk);
		} else if (countryName.contains("Sudan")) {
			holder.flag.setImageResource(R.drawable.sd);
		} else if (countryName.contains("Suriname")) {
			holder.flag.setImageResource(R.drawable.sr);
		} else if (countryName.contains("Swaziland")) {
			holder.flag.setImageResource(R.drawable.sz);
		} else if (countryName.contains("Sweden")) {
			holder.flag.setImageResource(R.drawable.se);
		} else if (countryName.contains("Switzerland")) {
			holder.flag.setImageResource(R.drawable.ch);
		} else if (countryName.contains("Syria")) {
			holder.flag.setImageResource(R.drawable.sy);
		} else if (countryName.contains("Taiwan")) {
			holder.flag.setImageResource(R.drawable.tw);
		} else if (countryName.contains("Tajikistan")) {
			holder.flag.setImageResource(R.drawable.tj);
		} else if (countryName.contains("Tanzania")) {
			holder.flag.setImageResource(R.drawable.tz);
		} else if (countryName.contains("Thailand")) {
			holder.flag.setImageResource(R.drawable.th);
		} else if (countryName.contains("Togo -")) {
			holder.flag.setImageResource(R.drawable.tg);
		} else if (countryName.contains("Tonga -")) {
			holder.flag.setImageResource(R.drawable.to);
		} else if (countryName.contains("Trinidad and Tobago")) {
			holder.flag.setImageResource(R.drawable.tt);
		} else if (countryName.contains("Tunisia")) {
			holder.flag.setImageResource(R.drawable.tn);
		} else if (countryName.contains("Turkey")) {
			holder.flag.setImageResource(R.drawable.tr);
		} else if (countryName.contains("Turkmenistan")) {
			holder.flag.setImageResource(R.drawable.tm);
		} else if (countryName.contains("Turks and Caicos")) {
			holder.flag.setImageResource(R.drawable.tc);
		} else if (countryName.contains("Tuvalu")) {
			holder.flag.setImageResource(R.drawable.tv);
		} else if (countryName.contains("Uganda")) {
			holder.flag.setImageResource(R.drawable.ug);
		} else if (countryName.contains("Ukraine")) {
			holder.flag.setImageResource(R.drawable.ua);
		} else if (countryName.contains("United Arab Emirates")) {
			holder.flag.setImageResource(R.drawable.ae);
		} else if (countryName.contains("United Kingdom")) {
			holder.flag.setImageResource(R.drawable.gb);
		} else if (countryName.contains("United States")) {
			holder.flag.setImageResource(R.drawable.us);
		} else if (countryName.contains("Uruguay")) {
			holder.flag.setImageResource(R.drawable.uy);
		} else if (countryName.contains("US Virgin Islands")) {
			holder.flag.setImageResource(R.drawable.vg);
		} else if (countryName.contains("Uzbekistan")) {
			holder.flag.setImageResource(R.drawable.uz);
		} else if (countryName.contains("Vanuatu")) {
			holder.flag.setImageResource(R.drawable.vu);
		} else if (countryName.contains("Venezuela")) {
			holder.flag.setImageResource(R.drawable.ve);
		} else if (countryName.contains("Vietnam")) {
			holder.flag.setImageResource(R.drawable.vn);
		} else if (countryName.contains("Wallis and Futuna")) {
			holder.flag.setImageResource(R.drawable.fr);
		} else if (countryName.contains("Western Samoa")) {
			holder.flag.setImageResource(R.drawable.eh);
		} else if (countryName.contains("Yemen")) {
			holder.flag.setImageResource(R.drawable.ye);
		} else if (countryName.contains("Zambia")) {
			holder.flag.setImageResource(R.drawable.zm);
		} else if (countryName.contains("Zimbabwe")) {
			holder.flag.setImageResource(R.drawable.zw);
		}
	}
}

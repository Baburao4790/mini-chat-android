package com.montycall.android.lebanoncall;

import java.util.ArrayList;


public class XmppPhoneContact implements IXmppPhoneContact, Comparable<XmppPhoneContact> {
	
	public String name = null;
	
	public ArrayList<String> phoneNumber = null; 
	
	public String photo_id = null;
	
	public String montyChatUserNumber = null;
	
	public boolean isRosterEntry = false;
	
	public String statusMode = null;
	
	public String message = null;
	
	public String jid = null;
	
	public byte[] avatar = null;
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getRosterName() {
		return rosterName;
	}

	public void setRosterName(String rosterName) {
		this.rosterName = rosterName;
	}


	public String rosterName = null;
	
	public int unreadcount = 0;
	
	public int getUnreadcount() {
		return unreadcount;
	}

	public void setUnreadcount(int unreadcount) {
		this.unreadcount = unreadcount;
	}

	public XmppPhoneContact() {
		name = null;
		phoneNumber = null; 
		photo_id = null;
		montyChatUserNumber = null;
	}
	
	public XmppPhoneContact(XmppPhoneContact contact) {
		
		if(contact.name != null) {
			name = new String(contact.name);
		}
		if(contact.photo_id != null) {
			photo_id = new String(contact.photo_id);
			
		}
		if(contact.montyChatUserNumber != null) {
			montyChatUserNumber = new String(contact.montyChatUserNumber);
			montyChatUserNumber = contact.montyChatUserNumber.substring(0, contact.montyChatUserNumber.length());
		}
		
		if(contact.phoneNumber != null) {
			phoneNumber = new ArrayList<String> (contact.phoneNumber.size());
			for(String item : contact.phoneNumber) {
				phoneNumber.add(new String(item));
			}
		}
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<String> getPhoneNumber() {
		return phoneNumber;
	}
	
	public void setIsRosterEntry(boolean isRosterEntry) {
		this.isRosterEntry = isRosterEntry;
	}
	
	public void setStatusMode(String statusMode) {
		this.statusMode = statusMode;
	}

	public void setPhoneNumber(ArrayList<String> phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getPhotoId() {
		return photo_id;
	}

	public void setPhotoId(String photo_id) {
		this.photo_id = photo_id;
	}

	public String getIsMontyChatUser() {
		return montyChatUserNumber;
	}

	public void setIsMontyChatUser(String montyChatUserNumber) {
		this.montyChatUserNumber = montyChatUserNumber;
	}
	
	public boolean getIsRosterEntry() {
		return this.isRosterEntry;
	}
	
	public String getStatusMode() {
		return this.statusMode;
	}
	
	public boolean isSectionItem() {
		return false;
	}
	
	
	@Override
	public int compareTo(XmppPhoneContact another) {
		if(this.getName() != null) {
			return this.getName().compareTo(another.getName());
		}
		return -1;
	}
	
	
}

package com.montycall.android.lebanoncall;

public class XmppPhoneContactSections implements IXmppPhoneContact {

	private char sectionLetter;


	public char getSectionLetter() {
		return sectionLetter;
	}


	public void setSectionLetter(char sectionLetter) {
		this.sectionLetter = sectionLetter;
	}


	public boolean isSectionItem() {
		return true;
	}

}

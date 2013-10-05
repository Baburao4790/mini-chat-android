package com.montycall.android.lebanoncall;


public class CallLogEntry {
	public String number;
	
	public String name;
	
	public int type;
	
	public long last_call_date = 0;
	
	public long last_call_duration = 0;

	@Override
	public boolean equals(Object o) {
		CallLogEntry other = (CallLogEntry)o;
		if(this.number.equalsIgnoreCase(other.number)) {
			return true;
		}
		return false;
	}

	public int compareTo(CallLogEntry another) {
		if(this.last_call_date == another.last_call_date) {
			return 0;
		} else if(this.last_call_date < another.last_call_date) {
			return 1;
		} else if(this.last_call_date > another.last_call_date){
			return -1;
		} else {
			return 0;
		}
	}	

}

package com.example.load;

public class Record
{
	private String id;
	private String eventName;


	public Record()
	{
	}

	public Record(
			String id,
			String eventName
	)
	{
		this.id = id;
		this.eventName = eventName;
	}

	public String getId()
	{
		return this.id;
	}

	public String getEventName()
	{
		return this.eventName;
	}
}

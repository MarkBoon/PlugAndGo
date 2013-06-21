package tesuji.games.general;

public class Property
{
	private String name;
	private String value;
	
	public Property() {}

	public Property(String name, String value)
	{
		this.name = name;
		this.value = value;
	}
	
	public Property(String name, int value)
	{
		this.name = name;
		this.value = Integer.toString(value);
	}
	
	public Property(String name, double value)
	{
		this.name = name;
		this.value = Double.toString(value);
	}
	
	public Property(String name, boolean value)
	{
		this.name = name;
		this.value = Boolean.toString(value);
	}
	
	public String getName()
    {
	    return name;
    }
	public void setName(String name)
    {
	    this.name = name;
    }
	public String getValue()
    {
	    return value;
    }
	public void setValue(String value)
    {
	    this.value = value;
    }
}

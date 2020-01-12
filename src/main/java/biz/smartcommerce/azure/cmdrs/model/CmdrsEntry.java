package biz.smartcommerce.azure.cmdrs.model;

import lombok.Data;
import lombok.experimental.Builder;

@Data
public class CmdrsEntry {
    public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public boolean isComplete() {
		return complete;
	}
	public void setComplete(boolean complete) {
		this.complete = complete;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private String category;
    private boolean complete;
    private String id;
    private String name;
}

package adu.app.photobeauty.entity;

public class GalleryRootObject {

	private String sdcardPath;
	private String thumbPath;
	private int numChild;
	private int order;

	public GalleryRootObject() {
	}

	public void setSdCardPath(String value) {
		this.sdcardPath = value;
	}

	public String getSdCardPath() {
		return this.sdcardPath;
	}

	public void setThumbPath(String value) {
		this.thumbPath = value;
	}

	public String getThumbPath() {
		return this.thumbPath;
	}

	public void setNumChild(int value) {
		this.numChild = value;
	}

	public int getNumChild() {
		return this.numChild;
	}

	public void setOrder(int value) {
		this.order = value;
	}

	public int getOrder() {
		return this.order;
	}
}

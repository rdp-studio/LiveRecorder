package cn.daniellee.plugin.lr.model;

public class PlayerData {

	private String name;

	private boolean denied;

	private int times;

	public PlayerData(String name) {
		this.name = name;
		this.denied = false;
		this.times = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isDenied() {
		return denied;
	}

	public void setDenied(boolean denied) {
		this.denied = denied;
	}

	public int getTimes() {
		return times;
	}

	public void setTimes(int times) {
		this.times = times;
	}
}

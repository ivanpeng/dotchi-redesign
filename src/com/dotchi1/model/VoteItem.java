package com.dotchi1.model;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.dotchi1.backend.json.BooleanDeserializer;
import com.dotchi1.backend.json.MedalTypeDeserializer;

public class VoteItem extends GameCardItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	private int votes;
	private float percent;
	@JsonDeserialize(using = MedalTypeDeserializer.class)
	private MedalType medals;
	@JsonDeserialize(using = BooleanDeserializer.class)
	private boolean finalItem;

	public int getVotes() {
		return votes;
	}

	public void setVotes(int votes) {
		this.votes = votes;
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}

	public MedalType getMedals() {
		return medals;
	}

	public void setMedals(MedalType medals) {
		this.medals = medals;
	}

	public boolean getFinalItem() {
		return finalItem;
	}

	public void setFinalItem(boolean finalItem) {
		this.finalItem = finalItem;
	}

	public enum MedalType	{
		GOLD("gold"),
		SILVER("silver"),
		COPPER("copper"),
		NONE("");
		
		private final String id;
		
		MedalType(String id)	{
			this.id = id;
		}
		public String getValue()	{
			return id;
		}
	}

	@Override
	public String toString() {
		return "VoteItem [gameItemId=" + gameItemId + ", itemImage="
				+ itemImage + ", itemTitle=" + itemTitle + ", itemContent="
				+ itemContent + ", votes=" + votes + ", percent=" + percent
				+ ", isDate=" + isDate + ", medals=" + medals + ", finalItem="
				+ finalItem + "]";
	}
	
	
}

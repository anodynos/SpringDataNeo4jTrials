package sdnTests.domain;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;


@NodeEntity
public class VideoContent extends Content {
	
	public VideoContent(){};
	public VideoContent(String title, String descr) {super(title, descr);}	
	
	private int duration;	
	public int getDuration() {return duration; }
	public void setDuration(int duration) { this.duration = duration; }
	
	public String toString() {
		return this.getClass().getSimpleName() + " title=" + title + ", duration = " + duration; 
	}
	
}

package sdnTests.domain;

import java.util.Set;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;


@NodeEntity
public class Person  {
	public Person(String name) { this.name = name; }

	@Indexed
	String name;	
	public String getName() {return name; }
	public void setName(String name) { this.name = name;}

	@RelatedTo(elementClass = Person.class, type = "LIKES", direction = Direction.OUTGOING) /* direction=INCOMING doesnt improve things, Person elements are still wrongly retrieved via likedContent */ 
	private Set<Person> likedPersons;
	public Set<Person> getLikedPersons() {return likedPersons;}
	
	@RelatedTo(elementClass = Content.class, type = "LIKES", direction = Direction.OUTGOING)	
	private Set<Content> likedContent;
	public Set<Content> getLikedContent() { return likedContent; }
}

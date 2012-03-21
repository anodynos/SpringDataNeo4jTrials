package sdnTests.domain;

import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.GraphProperty;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class Content {	
	public Content(){};		
	public Content(String title, String text) {this.title = title; this.descr=text;}	
	
	@Indexed(indexName="contentDescriptions", indexType=IndexType.FULLTEXT, level=Indexed.Level.CLASS )
	private String descr;	
	public String getDescr() {return descr; }
	public void setDescr(String descr) {this.descr = descr;}

	@Indexed(indexName="contentTitles", indexType=IndexType.FULLTEXT, level=Indexed.Level.GLOBAL)
	@Fetch public String title; //public deliberately - @see DomainTest.retrieved_object_properties_are_read_correctly	
	public String getTitle() { return title;}
	public void setTitle(String title) { this.title = title;}
	
	//primitive based
	@Indexed(indexName="PrimitiveIds")
	private int primitiveId;	
	public int getPrimitiveId() { return primitiveId;}
	public void setPrimitiveId(int primitiveId) {this.primitiveId = primitiveId; }
	
	//Long based
	@Indexed(indexName="LongIds") 
	private Long longId;	
	public Long getLongId() { return longId;}
	public void setLongId(Long longId) {this.longId = longId; }
	
	//primitive based
	@Indexed (indexName="StringIds")
	private String stringId;	
	public String getStringId() { return stringId;}
	public void setStringId(String stringId) { this.stringId = stringId; }   
}

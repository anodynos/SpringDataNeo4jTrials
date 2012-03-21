
package sdnTests.test;

import java.util.ArrayList;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsIterableWithSize.iterableWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.hamcrest.collection.IsIterableWithSize;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.EndResult;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import sdnTests.domain.Content;
import sdnTests.domain.Person;
import sdnTests.domain.VideoContent;
import sdnTests.repository.ContentRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/test-context.xml"})
@Transactional
public class HamcrestIteratorSizeTest {	
	
	@Autowired
	protected ContentRepository contentRepository;
	
	public static int iterSize(Iterator iter){ int i=0; while (iter.hasNext()){ i++;iter.next();} return i;}	
	public static int iterSize(Iterable iter) {return iterSize(iter.iterator());}
	
	@Test	
	public void IsIterableWithSize_withCorrectSize() {						
		Content content = new Content("Simple content", "").persist();		
		VideoContent videoContent = new VideoContent("Video content", "").persist();		
		
		EndResult<Content> contents = contentRepository.findAllByQuery("title", "*conte*");
		/* works OK */
		assertThat("We have TWO items in the Iterator ", contents, IsIterableWithSize.<Content>iterableWithSize(2));		 
	}
	
	@Test	
	public void IsIterableWithSize_withWrongSize() {						
		Content content = new Content("Simple content","").persist();		
		VideoContent videoContent = new VideoContent("Video content", "").persist();
		
		EndResult<Content> contents = contentRepository.findAllByQuery("title", "*conte*");		
		
		/* produces wrong(?) message 
		 * 		java.lang.AssertionError:  
		 * 			Expected: an iterable with size <3> 
		 * 			got: <org.springframework.data.neo4j.conversion.QueryResultBuilder$1@1970ae0>
		 * 
		 * I guess it should say " got: an Iterable with size <3> "
		 * 
		 */
		assertThat("We have TWO items in the Iterator ", contents, IsIterableWithSize.<Content>iterableWithSize(3));
	}
}


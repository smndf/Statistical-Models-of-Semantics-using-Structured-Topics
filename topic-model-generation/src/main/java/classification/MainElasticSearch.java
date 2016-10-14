package classification;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.client.Client;
import org.elasticsearch.node.Node;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/**
 * Hello world!
 *
 */
public class MainElasticSearch 
{
	public static void main( String[] args )
	{
		DBPediaCrawler crawler = new DBPediaCrawler();
		ElasticSearch es = new ElasticSearch();
		Node node = nodeBuilder().node();
		Client client = node.client();
		System.out.print("Loading clusters... ");
		String inputFile = "/good_clusters.txt";
		String indexName = "topics";
		Integer esPort = 9250;
		Map<String,Set<String>> map = es.loadClusters(inputFile, indexName);
		System.out.println("OK");
		//StringWriter sw = new StringWriter();
		//File fo = new File("classification.txt");
		//try
		//{
			//FileWriter fw = new FileWriter (fo);
			//for (int i = 1; i<100; i++){			
				//System.out.print("abstract " + i + " : ");
				//String articleAbstract = crawler.getRandomAbstract();
				//sw.write("abstract " + i + " : " + articleAbstract + "\n");
				SearchHits hits = es.search("Contador Hincapie Bennati Bahamontez".toLowerCase(), indexName, client);
				for (SearchHit hit : hits.getHits()) {
					System.out.print("score:" + hit.getScore() + map.get(hit.getId()).toString()+ "\n");
					//sw.write("score:" + hit.getScore() + map.get(hit.getId()).toString() + "\n");
				}
				//sw.write ("\n");
				//fw.write (sw.toString());
				//sw.getBuffer().setLength(0);
			//}
			//fw.close();
		/*}
		catch (IOException exception)
		{
			System.out.println (exception.getMessage());
		}*/
		node.close();
	}
}

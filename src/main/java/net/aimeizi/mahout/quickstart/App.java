package net.aimeizi.mahout.quickstart;

import java.util.List;

import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.ClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.FarthestNeighborClusterSimilarity;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.RandomRecommender;
import org.apache.mahout.cf.taste.impl.recommender.TreeClusteringRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.ConjugateGradientOptimizer;
import org.apache.mahout.cf.taste.impl.recommender.knn.KnnItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.knn.Optimizer;
import org.apache.mahout.cf.taste.impl.recommender.slopeone.SlopeOneRecommender;
import org.apache.mahout.cf.taste.impl.recommender.svd.ALSWRFactorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.Factorizer;
import org.apache.mahout.cf.taste.impl.recommender.svd.SVDRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	//DataModel：DataModel 是用户喜好信息的抽象接口，它的具体实现支持从任意类型的数据源抽取用户喜好信息。
    	//Taste 默认提供 JDBCDataModel 和 FileDataModel，分别支持从数据库和文件中读取用户的喜好信息

    	//File file = new File(App.class.getClassLoader().getResource("dataset.csv").getPath());
    	//DataModel model = new FileDataModel(file);
    	
    	/**************使用JDBCDataModel*****************/
    	
    	MysqlDataSource dataSource = new MysqlDataSource();
    	dataSource.setDatabaseName("mahout");
    	dataSource.setServerName("localhost");
    	dataSource.setUser("root");
    	dataSource.setPassword("");
    	
    	JDBCDataModel jdbcDataModel=new MySQLJDBCDataModel(dataSource,"intro", "uid", "iid", "val", "time");
    	
    	DataModel model = jdbcDataModel;
    	
    	System.out.println("*****************基于用户相似度的推荐引擎*******************");	
    	userCF(model);
    	
    	System.out.println("*****************基于内容的推荐引擎*******************");
    	itemCF(model);
    	
    	System.out.println("*****************基于SVD的推荐引擎*******************");
    	svdCF(model);
    	
    	System.out.println("*****************基于Knn的推荐引擎*******************");
    	knnItemCF(model);
    	
    	System.out.println("*****************基于TreeCluster的推荐引擎*******************");
    	treeClusterCF(model);
    	
    	System.out.println("*****************基于SlopeOne的推荐引擎*******************");
    	slopeOneCF(model);
    	
    	System.out.println("*****************基于random的推荐引擎*******************");
    	randomCF(model);
    }
    
    
    
    /**
     * 基于用户相似度的推荐引擎
     * @param model
     * @throws Exception
     */
    private static void userCF(DataModel model) throws Exception{
    	
    	//UserSimilarity 用于定义两个用户间的相似度，它是基于协同过滤的推荐引擎的核心部分，可以用来计算用户的“邻居”，
    	//这里我们将与当前用户口味相似的用户称为他的邻居
    	UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
    	//UserNeighborhood 用于基于用户相似度的推荐方法中，推荐的内容是基于找到与当前用户喜好相似的“邻居用户”的方式产生的。
    	//UserNeighborhood 定义了确定邻居用户的方法，具体实现一般是基于 UserSimilarity 计算得到的
    	//UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
    	
    	//基于用户相似度计算用户的“邻居”，这里将与该用户最近距离为 3 的用户设置为该用户的“邻居”
    	
    	//近邻算法分为2种
    	//NearestNUserNeighborhood:指定N的个数，比如，选出前10最相似的用户。
    	//ThresholdUserNeighborhood:指定比例，比如，选择前10%最相似的用户。
    	UserNeighborhood neighborhood = new NearestNUserNeighborhood(3, similarity, model);
    	
    	//Recommender：Recommender 是推荐引擎的抽象接口，Taste 中的核心组件。
    	//程序中，为它提供一个 DataModel，它可以计算出对不同用户的推荐内容。
    	//实际应用中，主要使用它的实现类 GenericUserBasedRecommender 或者 GenericItemBasedRecommender，
    	//分别实现基于用户相似度的推荐引擎或者基于内容的推荐引擎
    	
    	//UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    	
    	//采用 CachingRecommender 为 RecommendationItem 进行缓存，从而提高访问速度
    	Recommender recommender = new CachingRecommender(new GenericUserBasedRecommender(model, neighborhood, similarity)); 
    	print(recommender);
    }
    
    
    /**
     * 基于内容的推荐引擎
     * @param model
     * @throws Exception
     */
    private static void itemCF(DataModel model) throws Exception{
    	//ItemSimilarity,计算内容之间的相似度
    	//EuclideanDistanceSimilarity  欧几里德相似度
    	//PearsonCorrelationSimilarity 皮尔逊相似度
    	ItemSimilarity itemSimilarity = new PearsonCorrelationSimilarity(model);
    	//采用 CachingRecommender 为 RecommendationItem 进行缓存，从而提高访问速度
    	Recommender recommender = new CachingRecommender(new GenericItemBasedRecommender(model, itemSimilarity));
    	print(recommender);
    }
    
    
    /**
     * 基于SVD算法的推荐引擎
     * @param model
     * @throws Exception
     */
    private static void svdCF(DataModel model) throws Exception{
    	Factorizer factorizer = new ALSWRFactorizer(model,3, 0.05f, 50);
		Recommender recommender = new CachingRecommender(new SVDRecommender(model, factorizer));
		print(recommender);
    }
    
    
    /**
     * 基于SlopeOne的推荐引擎
     * SlopeOneRecommender基于Slopeone算法的推荐器，
     * Slopeone算法适用于用户对item的打分是具体数值的情况。
     * Slopeone算法不同于前面提到的基于相似度的算法，
     * 他计算简单快速，对新用户推荐效果不错，数据更新和扩展性都很不错，
     * 预测能达到和基于相似度的算法差不多的效果，很适合在实际项目中使用。
     * @param model
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
	private static void slopeOneCF(DataModel model) throws Exception{
		Recommender recommender = new CachingRecommender(new SlopeOneRecommender(model));
		print(recommender);
    }
    
    
    /**
     * 基于Knn的推荐引擎
     * @param model
     * @throws Exception
     */
    @SuppressWarnings("deprecation")
	private static void knnItemCF(DataModel model) throws Exception{
    	ItemSimilarity pearsonCorrelationSimilarity = new PearsonCorrelationSimilarity(model);
    	Optimizer optimizer = new ConjugateGradientOptimizer();
		Recommender recommender = new CachingRecommender(new KnnItemBasedRecommender(model,pearsonCorrelationSimilarity,optimizer,5));
		print(recommender);
    }
    
    
    /**
     * 基于TreeCluster的推荐引擎
     * @param model
     * @throws Exception
     */
    private static void treeClusterCF(DataModel model) throws Exception{
    	UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
    	ClusterSimilarity clusterSimilarity = new FarthestNeighborClusterSimilarity(similarity);
    	Recommender recommender = new CachingRecommender(new TreeClusteringRecommender(model, clusterSimilarity, 2));
    	print(recommender);
    }
    
    
    
    /**
     * 基于Random算法的推荐引擎
     * 随机推荐item,  除了测试性能的时候有用外，没太大用处。
     * @param model
     * @throws Exception
     */
    private static void randomCF(DataModel model) throws Exception{
		Recommender recommender = new CachingRecommender(new RandomRecommender(model));
		print(recommender);
    }
    
    
    /**
     * 打印结果
     * @param recommend
     */
    private static void print(Recommender recommender)throws Exception{
    	List<RecommendedItem> recommendations = recommender.recommend(2,3);//参数为用户标识和推荐项的个数
    	for (RecommendedItem recommendation : recommendations) {
      	  System.out.println(recommendation.getItemID()+"\t"+recommendation.getValue());
      	}
    }
    
}

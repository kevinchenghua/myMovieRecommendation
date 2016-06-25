import java.io.*;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Collectors;
import weka.core.*;
import weka.core.neighboursearch.LinearNNSearch;

class Predictor {
  public static void main(String [] args){
    
    List<UserMovieInstance> userMovie = Preprocessor.readDataFromCSV("res\\dataset\\test.csv");
    List<UserMoviesInstance> userMovies = Preprocessor.joinUser(userMovie);
    TrainTestPair dataPair = Preprocessor.splitData(userMovies,0.01,1);
    
    ArrayList<Attribute> atts = generateMovieAttributes(userMovie);
    int attsAmount = atts.size();
    Instances dataRaw = new Instances("MoviesRating",atts,0);
    dataRaw.setClass(atts.get(0));
    
    System.out.println("Before adding any instance");
    System.out.println("--------------------------");
    
    for(UserMoviesInstance instance : dataPair.getTrainingData()) {
      dataRaw.add(new SparseInstance(1.0, instance.getRatingsDoubleArray(), instance.getMoviesIntArray(), attsAmount));
      System.out.println(instance.getUser());
    }
    
    int user = 9;
    Instance userData = new SparseInstance(1.0, dataPair.getTestingData().get(user).getRatingsDoubleArray(), dataPair.getTestingData().get(user).getMoviesIntArray(), 1000);
    System.out.println(dataPair.getTestingData().get(user));
    userData.setDataset(dataRaw);
    
    
    
    System.out.println("After adding a instance");
    System.out.println("--------------------------");
    
    LinearNNSearch kNN = new LinearNNSearch(dataRaw);
    Instances neighbors = null;
    double[] distances = null;
    
    try {
      neighbors = kNN.kNearestNeighbours(userData, 5);
      distances = kNN.getDistances();
      
      System.out.println(Arrays.toString(distances));
    }catch (Exception e) {
      System.out.println("Neighbors could not be found.");
      return;
    }
    for(int i = 0; i < neighbors.size(); i++) {
      System.out.println(neighbors.get(i).toStringNoWeight());
    }
    
    double[] similarities = new double[distances.length];
    for (int i = 0; i < distances.length; i++) {
      similarities[i] = 1.0 / distances[i];
    }
    
    Enumeration nInstances = neighbors.enumerateInstances();
    Map<String, List<Integer>> recommendations = new HashMap<String, List<Integer>>();
    for(int i = 0; i < neighbors.numInstances(); i++){
      Instance currNeighbor = neighbors.get(i);

      for (int j = 0; j < currNeighbor.numAttributes(); j++) {
        if (userData.value(j) < 1) {
          String attrName = userData.attribute(j).name();
          List<Integer> lst = new ArrayList<Integer>();
          if (recommendations.containsKey(attrName)) {
            lst = recommendations.get(attrName);
          }
          
          lst.add((int)currNeighbor.value(j));
          recommendations.put(attrName, lst);
        }
      }

    }
    List<RecommendationRecord> finalRanks = new ArrayList<RecommendationRecord>();

    Iterator<String> it = recommendations.keySet().iterator();
    while (it.hasNext()) {
      String atrName = it.next();
      double totalImpact = 0;
      double weightedSum = 0;
      List<Integer> ranks = recommendations.get(atrName);
      for (int i = 0; i < ranks.size(); i++) {
        int val = ranks.get(i);
        totalImpact += similarities[i];
        weightedSum += (double) similarities[i] * val;
      }
      RecommendationRecord rec = new RecommendationRecord(atrName, weightedSum / totalImpact);

      finalRanks.add(rec);
    }
    Collections.sort(finalRanks);

    // print top 3 recommendations
    System.out.println(finalRanks.get(0));
    System.out.println(finalRanks.get(1));
    System.out.println(finalRanks.get(2));
    System.out.println(finalRanks.get(finalRanks.size()-1));
  }
  private static int getMovieAmount(List<UserMovieInstance> list) {
    return Collections.max(list, new Comparator<UserMovieInstance>() {
      @Override
      public int compare(UserMovieInstance first, UserMovieInstance second) {
        if(first.getMovie() > second.getMovie())
          return 1;
        else if(first.getMovie() < second.getMovie()) 
          return -1;
        return 0;
      }
    }).getMovie();
  }
  private static ArrayList<Attribute> generateMovieAttributes(List<UserMovieInstance> list) {
    int movieAmount = getMovieAmount(list);
    ArrayList<Attribute> atts = new ArrayList<Attribute>(
                                    IntStream.range(1,movieAmount+1)
                                             .mapToObj(i -> new Attribute(Integer.toString(i)))
                                             .collect(Collectors.toList()));
    atts.add(0,new Attribute("class"));
    return atts;
  }
}
class RecommendationRecord implements Comparable<RecommendationRecord>{
  private String attributeName;
  private double score;
  
  public RecommendationRecord(String attributeName, double score) {
    this.attributeName = attributeName;
    this.score = score;
  }
  
  public double getScore() {
    return this.score;
  }
  
  @Override
  public int compareTo(RecommendationRecord other) {
    if(other.getScore() > this.score)
      return 1;
    else if(other.getScore() < this.score)
      return -1;
    return 0;
  }
  
  @Override
  public String toString() {
    return "Movie: " + attributeName + ", Score: " + score;
  }
}
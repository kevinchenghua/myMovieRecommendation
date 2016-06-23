import java.io.BufferedReader;
import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Preprocessor {
  /*
  * This is a method to read data from MovieTweeting.csv and record with
  * UserMovieInstances
  */
  public static List<UserMovieInstance> readDataFromCSV(String fileName) {
    List<UserMovieInstance> data = new ArrayList();
    Path pathToFile = Paths.get(fileName);
    
    try (BufferedReader br = Files.newBufferedReader(pathToFile,
      StandardCharsets.US_ASCII)) {
      
      //read the first line (attributes)
      String line = br.readLine();
      line = br.readLine();
      while (line != null) {
        String[] attributes = line.split(",");
        UserMovieInstance instance = createInstance(attributes);
        data.add(instance);
        line = br.readLine();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    
    return data;
  }
  /*
  * This is a helper method to create UserMovieInstance for readDataFromCSV
  */
  private static UserMovieInstance createInstance(String[] metadata) {
    int user = Integer.parseInt(metadata[0]);
    int movie = Integer.parseInt(metadata[1]);
    int rating = Integer.parseInt(metadata[2]);
    
    return new UserMovieInstance(user, movie, rating);
  }
  /*
  * This is a method to join UserMovieInstances to UserMoviesInstance
  */
  public static List<UserMoviesInstance> joinUser(List<UserMovieInstance> data) {
    Map<Integer, UserMoviesInstance> reducedUserMap = 
      data.stream()
          .collect(
            Collectors.toMap(UserMovieInstance::getUser, UserMoviesInstance::new, UserMoviesInstance::merge));
    List<UserMoviesInstance> reducedUserList = new ArrayList<UserMoviesInstance>(reducedUserMap.values());
    return reducedUserList;
  }
  /*
  * This is a method to split data to training set and testing set.
  * @param splitPercentage: a value between 0 and 1, to partition 
  *   data with that proportion for traing data, and the rest for testing data
  * @param seed: a number for random seed to reproduce partition result
  */
  public static TrainTestPair splitData(List<UserMoviesInstance> data, double splitPercentage, long seed) {
    if(splitPercentage > 1 || splitPercentage < 0) {
      throw new IllegalArgumentException("splitPercentage must between 0 and 1");
    }
    //randomize the list
    Collections.shuffle(data,new Random(seed));
    
    ArrayList<UserMoviesInstance> train = new ArrayList<UserMoviesInstance>();
    ArrayList<UserMoviesInstance> test = new ArrayList<UserMoviesInstance>();
    
    // split the data to training part
    int trainSize = (int)(data.size()*splitPercentage);
    train.addAll(data.subList(0, trainSize));
    // split the data to testing part
    test.addAll(data.subList(trainSize, data.size()));
    
    return new TrainTestPair(train, test);
  }
}
/*
* The class UserMovieInstance is used to record the data import from
* MovieTweeting, whose format is:
*   user-movie-rating
*/
class UserMovieInstance {
  private int user;
  private int movie;
  private int rating;
  
  public UserMovieInstance(int user, int movie, int rating) {
    this.user = user;
    this.movie = movie;
    this.rating = rating;
  }
  
  public int getUser() {
    return user;
  }
  
  public int getMovie() {
    return movie;
  }
  
  public int getRating() {
    return rating;
  }
  
  @Override
  public String toString() {
    return "U:" + user + "\tM:" + movie + "\tR:" + rating;
  }
}
/*
* The class UserMoviesInstance record the data to use in weka sparse instance.
* And the format is:
*   user-movies[]-ratings[]
*/
class UserMoviesInstance {
  private int user;
  private List<Integer> movies;
  private List<Integer> ratings;
  
  /*
  * This is a constructor for valueMapper in Collectors.toMap to join UserMovieInstances.
  */
  public UserMoviesInstance(UserMovieInstance usermovieinstance) {
    this.user = usermovieinstance.getUser();
    this.movies = new ArrayList<>(Arrays.asList(usermovieinstance.getMovie()));
    this.ratings = new ArrayList<>(Arrays.asList(usermovieinstance.getRating()));
  }
  
  /*
  * This is a helper method for mergeFunction in Collectors.toMap to join UserMovieInstances.
  */ 
  public UserMoviesInstance merge(UserMoviesInstance that) {
    movies.addAll(that.getMovies());
    ratings.addAll(that.getRatings());
    return this;
  }
  
  public int getUser() {
    return this.user;
  }
  
  public List<Integer> getMovies() {
    return this.movies;
  }
  
  public List<Integer> getRatings() {
    return this.ratings;
  }
  
  @Override
  public String toString() {
    String s = "User:\t";
    s += Integer.toString(user) + "\nMovies:\t";
    for(Integer movie : movies) {
      s += Integer.toString(movie) + "\t";
    }
    s += "\nRating:\t";
    for(Integer rating : ratings) {
      s += Integer.toString(rating) + "\t";
    }
    return s;
  }
}

class TrainTestPair {
  private List<UserMoviesInstance> train;
  private List<UserMoviesInstance> test;
  
  public TrainTestPair(List<UserMoviesInstance> train, List<UserMoviesInstance> test) {
    this.train = train;
    this.test = test;
  }
  
  public List<UserMoviesInstance> getTrainingData() {
    return this.train;
  }
  
  public List<UserMoviesInstance> getTestingData() {
    return this.test;
  }
}
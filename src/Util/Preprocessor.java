import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Preprocessor {
  public static void main(String [] args){
    
    List<UserMovieInstance> userMovie = readDataFromCSV("..\\..\\dataset\\test.csv");
    List<UserMoviesInstance> userMovies = joinUser(userMovie);
    
    for(UserMoviesInstance instance : userMovies) {
      System.out.println(instance);
    }
  }
  
  private static List<UserMovieInstance> readDataFromCSV(String fileName) {
    List<UserMovieInstance> data = new ArrayList();
    Path pathToFile = Paths.get(fileName);
    
    try (BufferedReader br = Files.newBufferedReader(pathToFile,
      StandardCharsets.US_ASCII)) {
      
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
  
  private static UserMovieInstance createInstance(String[] metadata) {
    int user = Integer.parseInt(metadata[0]);
    int movie = Integer.parseInt(metadata[1]);
    int rating = Integer.parseInt(metadata[2]);
    
    return new UserMovieInstance(user, movie, rating);
  }
  
  private static List<UserMoviesInstance> joinUser(List<UserMovieInstance> data) {
    Map<Integer, UserMoviesInstance> reducedUserMap = 
      data.stream()
          .collect(
            Collectors.toMap(UserMovieInstance::getUser, UserMoviesInstance::new, UserMoviesInstance::merge));
    List<UserMoviesInstance> reducedUserList = new ArrayList<UserMoviesInstance>(reducedUserMap.values());
    return reducedUserList;
  }
}

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

class UserMoviesInstance {
  private int user;
  private List<Integer> movies;
  private List<Integer> ratings;
  
  public UserMoviesInstance(UserMovieInstance usermovieinstance) {
    this.user = usermovieinstance.getUser();
    this.movies = new ArrayList<>(Arrays.asList(usermovieinstance.getMovie()));
    this.ratings = new ArrayList<>(Arrays.asList(usermovieinstance.getRating()));
  }
  
  public UserMoviesInstance merge(UserMoviesInstance that) {
    movies.addAll(that.getMovies());
    ratings.addAll(that.getRatings());
    return this;
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
import java.io.*;
import java.util.*;
import weka.core.*;

class Predictor {
  public static void main(String [] args){
    
    List<UserMovieInstance> userMovie = Preprocessor.readDataFromCSV("..\\dataset\\test.csv");
    List<UserMoviesInstance> userMovies = Preprocessor.joinUser(userMovie);
    TrainTestPair dataPair = Preprocessor.splitData(userMovies,0.001,1);
    
    for(UserMoviesInstance instance : abc.getTrainingData()) {
      System.out.println(instance);
    }
  }
}
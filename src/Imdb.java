import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import bean.Actor;
import bean.ActorsInMovies;
import bean.Director;
import bean.DirectorsOfMovies;
import bean.Movie;
import bean.MyRatings;
import bean.UserRating;

public class Imdb {
	private static ArrayList<Actor> actorList = new ArrayList<>();
	private static ArrayList<ActorsInMovies> actorsInMoviesList = new ArrayList<>();
	private static ArrayList<Director> directorList = new ArrayList<>();
	private static ArrayList<DirectorsOfMovies> directorsOfMovieList = new ArrayList<>();
	private static ArrayList<Movie> movieList = new ArrayList<>();
	private static ArrayList<UserRating> userRatingList = new ArrayList<>();
	private static ArrayList<MyRatings> myRatingsList = new ArrayList<>();

	public static void main(String[] args) {
		System.out.println("Loading Data....!");
		loadMyRatings();
		loadData();
		System.out.println("Data Loaded Successfully...!");

		if (args.length > 0) {
			String query = "";
			for (String param : args) {
				query += param + ",";
			}

			if (query.contains("--test=true")) {
				writeTestQueryToFile();
			} else {
				ArrayList<Movie> allSearchedMovies = new ArrayList<>();
				int limit = -1;
				String[] queries = query.split(",");

				for (String key : queries) {
					if (key.contains("--genre")) {
						String genre = key.split("=")[1];
						allSearchedMovies.addAll(getAllGenreMovies(genre));
					} else if (key.contains("--actor")) {
						String actor = key.split("=")[1];
						String actors[] = actor.split(",");
						int actorsIds[] = new int[actors.length];
						
						
						//get All Actors Ids
						for(int i = 0; i < actors.length; i++) {
							actorsIds[i] = getActorId(actors[i]);
						}
						
						ArrayList<Integer> moviesIds = new ArrayList<Integer>();
						
						//get all movies ids
						for(int i = 0; i < actors.length; i++) {
							if(actorsIds[i] > 0) {
								getActorMovies(moviesIds, actorsIds[i]);
							}
						}
						
						
						//get all movies
						for(Integer id : moviesIds) {
							allSearchedMovies.add(getSearchedMovie(id));
						}
						
						
					} else if (key.contains("--director")) {
						String director = key.split("=")[1];
						String directors[] = director.split(",");
						int directorsIds[] = new int[directors.length];
						
						
						//get All Actors Ids
						for(int i = 0; i < directors.length; i++) {
							directorsIds[i] = getDirectorId(directors[i]);
						}
						
						ArrayList<Integer> moviesIds = new ArrayList<Integer>();
						
						//get all movies ids
						for(int i = 0; i < directors.length; i++) {
							if(directorsIds[i] > 0) {
								getDirectorMovies(moviesIds, directorsIds[i]);
							}
						}
						
						//getall movies
						for(Integer id : moviesIds) {
							allSearchedMovies.add(getSearchedMovie(id));
						}
					} else if (key.contains("film")) {
						String film = key.split("=")[1];
						double rating = getSearchedMovie(film).getRating();

						filterMoviesOnRating(allSearchedMovies, rating);
					} else if (key.contains("--limit")) {
						limit = Integer.parseInt(key.split("=")[1]);
					}
				}
				if (limit != -1) {
					if (allSearchedMovies.size() > limit) {
						System.out.println(allSearchedMovies.subList(0, limit));
					}
					else {
						System.out.println(allSearchedMovies);
					}
					
				} else if (allSearchedMovies.size() > 200) {
					System.out.println(allSearchedMovies.subList(0, 200));
				}

				System.out.println(allSearchedMovies);
			}
		}

		Scanner input = new Scanner(System.in);

		boolean toContinue = true;
		int index = 0;
		do {
			index = 0;
			System.out.println(++index + ". Search Movie");
			System.out.println(++index + ". Rate Movie");
			System.out.println(++index + ". Recommend Movie");
			System.out.println(++index + ". Exit");
			int choice = input.nextInt();
			input.nextLine();
			switch (choice) {
			case 1:
				System.out.println("Enter Movie Name e.g Matrix ****");
				searchMovie(input.nextLine());
				break;
			case 2:
				System.out.println("Enter Movie Name for Ratinge.g Matrix ****");
				Movie movie = getSearchedMovie(input.nextLine());
				if (movie == null) {
					System.err.println("No Movie Found..");
				} else {
					System.out.println("Rate Movie (1- bad , 5-Good)");
					int rating = input.nextInt();
					input.nextLine();
					MyRatings userRating = new MyRatings();
					userRating.setUsername("Me");
					userRating.setMovieId(movie.getId());
					userRating.setRating(rating);
					myRatingsList.add(userRating);
				}

				break;
			case 4:
				toContinue = false;
				saveMyRatings();
				break;
			case 3:
				recommendMovie();
				break;
			}
		} while (toContinue);

	}

	private static void getDirectorMovies(ArrayList<Integer> moviesIds, int i) {
		for(DirectorsOfMovies directorOfMovies : directorsOfMovieList) {
			if(directorOfMovies.getDirectorId() == i) {
				moviesIds.add(directorOfMovies.getMovieId());
			}
		}
		
	}

	private static int getDirectorId(String directorName) {
		for(Director director : directorList) {
			if(director.getName().toLowerCase().equals(directorName.toLowerCase()))
				return director.getId();
		}
		return 0;
	}

	private static Movie getSearchedMovie(Integer id) {
		for(Movie movie : movieList) {
			if(movie.getId() == id)
				return movie;
		}
		return null;
	}

	private static void filterMoviesOnRating(ArrayList<Movie> allSearchedMovies, double rating) {

		ArrayList<Movie> temp = new ArrayList<>(0);
		for (Movie movie : allSearchedMovies) {
			if (movie.getRating() == rating) {
				temp.add(movie);
			}
		}

		allSearchedMovies.clear();
		allSearchedMovies.addAll(temp);
	}

	private static ArrayList<Movie> getAllGenreMovies(String genre) {
		ArrayList<Movie> temp = new ArrayList<>();
		for (Movie movie : movieList) {
			if (movie.getGenre().toLowerCase().contains(genre.toLowerCase()))
				temp.add(movie);
		}
		return temp;
	}

	private static void writeTestQueryToFile() {
		ArrayList<Movie> queryOneMovies = new ArrayList<>();

		// query no. 1
		String genre = "thriller";
		int limit = 10;
		String movieName = "Matrix Revolutions";

		Movie temp = null;
		for (Movie movie : movieList) {
			if (movie.getName().toLowerCase().contains(movieName.toLowerCase())) {
				temp = movie;
				break;
			}
		}

		if (temp == null)
			return;
		int length = 0;
		for (Movie movie : movieList) {
			if ((movie.getRating() > temp.getRating() - 0.3 && movie.getRating() < temp.getRating() + 0.3)
					&& movie.getGenre().toLowerCase().equals(genre.toLowerCase())) {
				queryOneMovies.add(movie);
				if (length++ > limit)
					break;
			}
		}

		ArrayList<Movie> queryTwoMovies = new ArrayList<>();
		// query no. 2
		genre = "Adventure";
		limit = 15;
		movieName = "Indiana Jones and the Temple of Doom";

		temp = null;
		for (Movie movie : movieList) {
			if (movie.getName().toLowerCase().contains(movieName.toLowerCase())) {
				temp = movie;
				break;
			}
		}

		// System.out.print(temp);
		// if (temp == null)
		// return;
		length = 0;
		for (Movie movie : movieList) {
			if ((movie.getRating() > temp.getRating() - 0.3 && movie.getRating() < temp.getRating() + 0.3)
					&& movie.getGenre().toLowerCase().equals(genre.toLowerCase())) {
				queryTwoMovies.add(movie);
				if (length++ > limit)
					break;
				;
			}
		}

		// query no. 3
		String actorOne = "Jason Statham", actorTwo = "Keanu Reeves";
		// get actor ids
		int actorOneId = getActorId(actorOne);
		int actorTwoId = getActorId(actorTwo);

		// System.out.println(actorOneId + " " + actorTwoId);

		ArrayList<Integer> actorOneMoviesIds = new ArrayList<>();

		// get Actor Movies
		getActorMovies(actorOneMoviesIds, actorOneId);
		getActorMovies(actorOneMoviesIds, actorTwoId);

		// getMoviesWithGenre
		genre = "Action";

		ArrayList<Movie> actorMovies = new ArrayList<>();
		for (int movieId : actorOneMoviesIds) {
			Movie movie = getMovie(movieId, genre);
			if (movie != null && actorMovies.size() < 50)
				actorMovies.add(movie);
		}

		String query = "QueryNo. 1 Result\n";
		for (Movie movie : queryOneMovies) {
			query += movie + "\n";
		}

		query += "QueryNo. 2 Result\n";
		for (Movie movie : queryTwoMovies) {
			query += movie + "\n";
		}

		query += "QueryNo. 3 Result\n";
		for (Movie movie : actorMovies) {
			query += movie + "\n";
		}

		writeToFile(query);

	}

	private static Movie getMovie(int movieId, String genre) {
		for (Movie movie : movieList) {
			if (movie.getId() == movieId && movie.getGenre().toLowerCase().equals(genre.toLowerCase()))
				return movie;
		}

		return null;
	}

	private static void getActorMovies(ArrayList<Integer> actorOneMoviesIds, int actorId) {

		for (ActorsInMovies actorInMovie : actorsInMoviesList) {
			if (actorInMovie.getActorId() == actorId)
				actorOneMoviesIds.add(actorInMovie.getMovieId());
		}

	}

	private static int getActorId(String actorName) {
		for (Actor actor : actorList) {
			if (actor != null)
				if (actor.getName().toLowerCase().equals(actorName.toLowerCase()))
					return actor.getId();
		}

		return -1;
	}

	private static void writeToFile(String query) {
		BufferedWriter bw = null;
		FileWriter fw = null;

		try {

			// String content = "This is the content to write into file\n";

			fw = new FileWriter("results.txt");
			bw = new BufferedWriter(fw);

			bw.write(query.trim());

			// System.out.println("Done");

		} catch (IOException e) {

			e.printStackTrace();

		} finally {

			try {

				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();

			} catch (IOException ex) {

				ex.printStackTrace();

			}

		}

	}

	private static void recommendMovie() {

		double sum = 0;
		for (MyRatings rating : myRatingsList) {
			sum += rating.getRating();
		}

		getMovie(sum / myRatingsList.size());

	}

	private static void getMovie(double rating) {
		int limit = 0;
		for (Movie movie : movieList) {
			if (movie.getRating() > rating - 0.15 && movie.getRating() < rating + 0.15) {
				System.out.println(movie);
				if (limit++ > 100)
					return;
			}
		}

	}

	private static void searchMovie(String movieName) {
		for (Movie movie : movieList) {
			if (movie.getName().toLowerCase().contains(movieName.toLowerCase()))
				System.out.println(movie);
		}
	}

	private static Movie getSearchedMovie(String movieName) {
		for (Movie movie : movieList) {
			if (movie.getName().toLowerCase().contains(movieName.toLowerCase())) {
				System.out.println(movie);
				return movie;
			}
		}
		return null;
	}

	private static void loadMyRatings() {
		FileInputStream fin = null;
		ObjectInputStream ois = null;

		try {

			fin = new FileInputStream("myRatings.txt");
			ois = new ObjectInputStream(fin);
			myRatingsList = (ArrayList<MyRatings>) ois.readObject();

		} catch (Exception ex) {
			// ex.printStackTrace();
		} finally {

			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}

			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					// e.printStackTrace();
				}
			}

		}

	}

	private static void saveMyRatings() {
		FileOutputStream fout = null;
		ObjectOutputStream oos = null;

		try {

			fout = new FileOutputStream("myRatings.txt");
			oos = new ObjectOutputStream(fout);
			oos.writeObject(myRatingsList);

			System.out.println("Done");

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			if (fout != null) {
				try {
					fout.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	private static void loadData() {
		try {
			FileReader fileReader = new FileReader("movieproject.txt");

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			int index = -1;
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.startsWith("New_Entity")) {
					index++;
					continue;
				}

				switch (index) {
				case 0:
					addActor(line);
					break;
				case 1:
					addMovie(line);
					break;
				case 2:
					addDirector(line);
					break;
				case 3:
					addActorInMovies(line);
					break;
				case 4:
					addDirectorsOfMovies(line);
					break;
				case 5:
					addUserRatingsRecord(line);
					break;
				}
			}

			// Always close files.
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addUserRatingsRecord(String line) {
		String parts[] = line.split(",");
		try {
			String username = parts[0].substring(1, parts[0].length() - 1);

			double rating = Double.parseDouble(parts[1].substring(1, parts[1].length() - 1));
			int movieId = Integer.parseInt(parts[2].substring(1, parts[2].length() - 1));
			UserRating userRating = new UserRating();
			userRating.setUsername(username);
			userRating.setRating(rating);
			userRating.setMovieId(movieId);

			userRatingList.add(userRating);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addDirectorsOfMovies(String line) {
		String parts[] = line.split(",");
		try {
			int directorId = Integer.parseInt(parts[0].substring(1, parts[0].length() - 1));
			int movieId = Integer.parseInt(parts[1].substring(1, parts[1].length() - 1));

			DirectorsOfMovies directorOfMovies = new DirectorsOfMovies();
			directorOfMovies.setDirectorId(directorId);
			directorOfMovies.setMovieId(movieId);

			directorsOfMovieList.add(directorOfMovies);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addActorInMovies(String line) {
		String parts[] = line.split(",");
		try {
			int actorId = Integer.parseInt(parts[0].substring(1, parts[0].length() - 1));
			int movieId = Integer.parseInt(parts[1].substring(1, parts[1].length() - 1));

			ActorsInMovies actorInMovies = new ActorsInMovies();
			actorInMovies.setActorId(actorId);
			actorInMovies.setMovieId(movieId);

			actorsInMoviesList.add(actorInMovies);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addDirector(String line) {
		String parts[] = line.split(",");
		try {
			int directorId = Integer.parseInt(parts[0].substring(1, parts[0].length() - 1));
			String directorName = parts[1].substring(1, parts[1].length() - 1);

			Director director = new Director();
			director.setId(directorId);
			director.setName(directorName);
			directorList.add(director);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void addMovie(String line) {
		String parts[] = line.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
		try {
			int index = 0;
			int movieId = Integer.parseInt(parts[index].substring(1, parts[index++].length() - 1));
			String movieName = parts[index].substring(1, parts[index++].length() - 1);
			String moviePlot = parts[index].substring(1, parts[index++].length() - 1);
			String genre = parts[index].substring(1, parts[index++].length() - 1);
			String releaseDate = parts[index].substring(1, parts[index++].length() - 1);
			;
			int votes = Integer.parseInt(parts[index].substring(1, parts[index++].length() - 1));
			double rating = Double.parseDouble(parts[index].substring(1, parts[index++].length() - 1));

			Movie movie = new Movie();
			movie.setId(movieId);
			movie.setName(movieName);
			movie.setGenre(genre);
			movie.setPlot(moviePlot);
			movie.setReleaseDate(releaseDate);
			movie.setVotes(votes);
			movie.setRating(rating);

			if (!containsMovie(movieId))
				movieList.add(movie);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private static void addActor(String line) {
		String parts[] = line.split(",");
		try {
			int actorId = Integer.parseInt(parts[0].substring(1, parts[0].length() - 1));
			String actorName = parts[1].substring(1, parts[1].length() - 1);

			Actor actor = new Actor();
			actor.setId(actorId);
			actor.setName(actorName);
			actorList.add(actor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean containsMovie(int id) {
		for (Movie movie : movieList) {
			if (movie.getId() == id)
				return true;
		}
		return false;
	}
}

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;


/**
 * @author Kevin Wood
 * @UID 111235445
 * 
 * I pledge on my honor that I have not given or received any assistance on this assignment.
 * Kevin Wood, kvn.wood@gmail.com / kwood123@terpmail.umd.edu
 * 
 * Overall strategy is - 
 * 	1. Read in the input, store in a hash ( the n-dimension matrix is created as well, but not used - 
 * 	   I changed my approach but left the matrix/tree construction in)
 * 	   The hash is faster and accesses game states more easily.
 *     Example of a hashed game state - h[023] = [3,6,7]. p0 played action 0 (first) & payoff 3, p1 played a3 & payoff of 6, p2 a4 & 7, etc.
 *  2. If not performing IESDS, print input (unchanged game state) then exit. otherwise:
 *  3. Have a loop that continually runs, attempting pure reduction then attempting mixed reduction.
 *     This loop runs until both attempts fail, meaning the matrix is fully reduced.
 *     Then print off the hash states of the fully reduced game and the remaining actions of each player.
 * 
 */




@SuppressWarnings("rawtypes")

public class IESDS {

	public static void main(String[] args) {
		int print = 0, pnum = 0;    /* prePrint is first integer read, whether to print before or after IESDS.
		 								  pnum is number of players, second integer read                       */

		Scanner sc = new Scanner(System.in);

		print = Integer.parseInt(sc.nextLine());     /* 0 or 1 */
		pnum = Integer.parseInt(sc.nextLine());      /* 1 <= pnum <= 5 */

		ArrayList<Integer> action_counts = new ArrayList<Integer>(pnum);
		ArrayList<ArrayList> payoffs = new ArrayList<ArrayList>(pnum); /* tree format of game, not used in solution though */

		for(int i = 0; i < pnum; i++){              /* each player has 1-10 actions possible */
			action_counts.add(Integer.parseInt(sc.next()));

		}
		sc.nextLine();

		// initializing payoff matrix and hash of game states
		Hashtable<String, ArrayList<Integer>> h = new Hashtable<String, ArrayList<Integer>>();
		construct_nD_Matrix(payoffs, pnum, action_counts, 1, sc, "", h);
		

		/* if just printing out game/input, exit afterwards */
		if(print == 0){
			Print_Game(h, pnum, action_counts);
			System.exit(0);
		}

		Hashtable<String, String> removed_strategies = new Hashtable<String,String>();
		boolean found_pure_reduction = true;
		boolean found_mixed_reduction = true;
		
		/*  Perform IESDS while in a loop that continuously runs until full reduction of game */
		while(found_pure_reduction || found_mixed_reduction){
			for(int player = 0; player < pnum; player++){
				found_pure_reduction = false;

				for(int action = 0; action < action_counts.get(player); action++){
					String s = String.valueOf(player) + String.valueOf(action);
					if(removed_strategies.containsKey(s)){
						continue;
					}

					ArrayList<String> in_set = new ArrayList<String>();
					Set<String> keys = h.keySet();
					ArrayList<String> out_sets = new ArrayList<String>();

					for(String key : keys){
						
						if(key.charAt(player) == Integer.toString(action).charAt(0)){
							in_set.add(key);
						} else{
							out_sets.add(key);
						}
					}

					/* separate out_set by string[player].
					 * so each different strategy for player has its own set.
					 */
					ArrayList<ArrayList<String>> other_strategies = new ArrayList<ArrayList<String>>(action_counts.get(player));
					for(int i = 0; i < action_counts.get(player); i++){
						ArrayList<String> t = new ArrayList<String>();
						other_strategies.add(t);
					}

					for(int i = 0; i < out_sets.size(); i++){

						char c =  out_sets.get(i).charAt(player);
						int index = Character.getNumericValue(c);
						other_strategies.get(index).add(out_sets.get(i));

					}

					// now we compare every key in which action was played to other strategies
					 
					for(int s2 = 0; s2 < other_strategies.size();s2++){

						String t = String.valueOf(player) + String.valueOf(s2);
						if(removed_strategies.containsKey(t))
							continue;
						boolean dominates = true;
						for(int j = 0; j < other_strategies.get(s2).size(); j++){

							/* obtaining game state to compare to */
							String key2 = other_strategies.get(s2).get(j);
							String path = "";
							for(int i2 = 0; i2 < key2.length(); i2++){
								if(i2 != player)
									path += key2.substring(i2, i2+1);
								else{
									path += action;

								}
							}
							
							String key1 = path;	
							int payoff_s1;
							int payoff_s2;

							/* if any of the constructed key strings are null in h, then break */
							if(h.get(key1) != null && h.get(key2) != null){
								payoff_s1 = h.get(key1).get(player);
								payoff_s2 = h.get(key2).get(player);
							} else{
								break;
							}

							/* if the current strategy payoff is less than it is for strategy s2, s1 does not dominate s2 */
							if(payoff_s1 <= payoff_s2){
								
								dominates = false;
								break;

							}

							if(!dominates)
								break;
						}
						
						/* removing the dominated action, putting it into the hash removed_Strategies */
						if(dominates && action != s2 && !in_set.isEmpty()){
							remove_strategy(h, player, action, s2);
							String tmp = String.valueOf(player) + String.valueOf(s2);
						
							removed_strategies.put(tmp, "1");
							found_pure_reduction = true;
							break;
						}
						
					}
				}
			}

			found_mixed_reduction = false;
			
			/* mixed reduction */
			for(int player = 0; player < pnum; player++){
				
				if(found_mixed_reduction)
					break;
				if(action_counts.get(player) < 3)
					continue;

				for(int action = 0; action < action_counts.get(player); action++){
					String s = String.valueOf(player) + String.valueOf(action); //key for strategy game state
					if(found_mixed_reduction)
						break;
					if(removed_strategies.containsKey(s))
						continue;
					
					/* building the two mixed strategies to compare against action */
					for(int mixed1 = 0; mixed1 < action_counts.get(player); mixed1++ ){
						
						String s2 = String.valueOf(player) + String.valueOf(mixed1); //key for mixed#1 game state
						if(mixed1 == action || removed_strategies.containsKey(s2))
							continue;
						if(found_mixed_reduction)
							break;
						
						for(int mixed2 = mixed1+1 ; mixed2 < action_counts.get(player); mixed2++){
							String s3 = String.valueOf(player) + String.valueOf(mixed2); // key for mixed#2 game state
							if(mixed2 == action || mixed2 == mixed1 || removed_strategies.containsKey(s3) ||
									s2.equals(s3))
								continue;

							ArrayList<String> in_set = new ArrayList<String>();
							Set<String> keys = h.keySet();
							ArrayList<String> out_sets = new ArrayList<String>();

							for(String key : keys){
								if(key.charAt(player) == Integer.toString(action).charAt(0)){
									in_set.add(key);
								} else{
									out_sets.add(key);
								}
							}

							/* loop p from .1 to 1, incrementing by .1 (probability for mixed1)
							 * q is 1-p	(probability for mixed2)
							 */
							for(double p = .1; p < 1; p+=.1){
								if(removed_strategies.containsKey(s3) || removed_strategies.containsKey(s2) ||
										removed_strategies.containsKey(s))
									break;
								boolean dominates = true;
								double q = 1-p;

								for(String path: in_set){
									
									double payoff_s1;
									if(h.get(path) != null)
										payoff_s1 = h.get(path).get(player);
									else{
										break;
									}

									String m1path = "";
									String m2path = "";

									// constructing paths (strings) for game states of each strategy in the mixed strategy
									for(int i2 = 0; i2 < path.length(); i2++){
										if(i2 != player){
											m1path += path.substring(i2, i2+1);
											m2path += path.substring(i2, i2+1);
										} else{
											m1path += mixed1;
											m2path += mixed2;
										}
									}

									// getting payoff for mixed strategy
									double payoff_s2;
									if(h.get(m1path) != null && h.get(m2path) != null){
										payoff_s2 = ((h.get(m1path).get(player)*p) + (h.get(m2path).get(player))*q);
									} else{
										break;
									}
									
									/* if payoff for strategy is greater than payoff for the mixed strategy,  
									 * then mixed strategy does not dominate s1 -> break
									 * 
									 * */
									if(payoff_s1 >= payoff_s2){
										dominates = false;
										break;
									}
								}

								/* mixed strategy dominates pure strategy, remove the pure strategy 
								 * and add to removed_strategies
								 */
								if(dominates){
									remove_strategy(h, player, -1, action);
									String tmp = String.valueOf(player) + String.valueOf(action);
									removed_strategies.put(tmp, "1");
									
									found_mixed_reduction = true;
									break;
								}

							}
						}
					}	
				}
			}
		}

		/* printing the game after full reduction */
		Print_Game(h, pnum, action_counts);

		sc.close();
	}

	/**
	 * @param h - hash of all game states
	 * @param pnum - number of players
	 * @param action_counts - number of actions for each player
	 * 
	 * Function prints full game state regardless of whether IESDS was performed or not.
	 */
	private static void Print_Game(Hashtable<String, ArrayList<Integer>> h, int pnum, ArrayList<Integer> action_counts) {
		System.out.println(pnum);

		Set<String> keys = h.keySet();
		ArrayList<ArrayList<String>> p_choices = new ArrayList<ArrayList<String>>(pnum);
		ArrayList<String> states = new ArrayList<String>();
		
		
		for(int i = 0; i < pnum; i++){
			ArrayList<String> tmp = new ArrayList<String>();
			p_choices.add(tmp);

		}

		//constructing an array of remaining choices for each player
		for(String k : keys){
			states.add(k);
			for(int index = 0; index < k.length(); index++){
				String p = k.substring(index, index+1);
				if(!p_choices.get(index).contains(p))
					p_choices.get(index).add(p);
			}

		}

		/* printing strategies for each player */
		for(int i = 0; i < pnum; i++){
			Collections.sort(p_choices.get(i));
			String p = "";
			for(String c: p_choices.get(i)){
				p += Integer.valueOf(c)+1 + " ";
			}
			
			System.out.println(p.trim());

		}

		Collections.sort(states);

		int p = 0;
		String tmp = "";
		int row_size = p_choices.get(pnum-1).size() * pnum;
		
		/* printing all game states in row-major order */
		for(int i = 0; i < states.size(); i++){
			if((p % row_size) == 0 && i != 0){
				System.out.println(tmp.trim());
				tmp = "";
			}

			ArrayList<Integer> payoffs = h.get(states.get(i));
			for (Integer x : payoffs){
				tmp += String.valueOf(x) + " ";
			}

			p+=pnum;

		}
		System.out.println(tmp.trim());

	}

	/**
	 * @param h - hash of all game states - e.g. "0102" -> [5,4,7,8]
	 * @param player - current player for which strategy is being removed.
	 * @param dominating - strategy which is dominating the dominated strategy (for debugging / information purposes)
	 * @param dominated - strategy which is dominated. All game states in which that action was played are removed from h.
	 */
	private static void remove_strategy(Hashtable<String, ArrayList<Integer>> h, int player, int dominating, int dominated) {
		Set<String> keys = h.keySet();
		ArrayList<String> to_remove = new ArrayList<String>();
		for(String key: keys){
			
			if(key.charAt(player) == Integer.toString(dominated).charAt(0)){
				to_remove.add(key);
			}
		}
		for(String k: to_remove){
			h.remove(k);
		}

	}

	/**
	 * @param payoffs - the payoff matrix (tree form)   (not used in final solution, I revised my approach. kept as a utility)
	 * @param pnum - the number of players in the game
	 * @param action_counts - number of actions each player has in the input
	 * @param depth - depth tracker for the payoff tree
	 * @param sc - the scanner, to keep reading in input
	 * @param path - keeps track of current game state, in the form of a string ex. "012" = p0a0, p1a1, p2a2
	 * @param h - hash of all possible game states, built recursively along with payoff tree. used in solution, rather than tree.
	
	 */
	@SuppressWarnings({"unchecked" })
	private static void construct_nD_Matrix(ArrayList<ArrayList> payoffs, int pnum,
			ArrayList<Integer> action_counts, int depth, Scanner sc, String path, Hashtable<String, ArrayList<Integer>> h) {

		/* if we are at the end, create an arraylist where each element is an integer arraylist, with payoffs in it (i.e. a cell).
		 * Each cell that is a payoff leaf is represented as an arraylist, with a_0 = player 1 payoff, a_1 = player 2 payoff etc.
		 * Put the completed path into the hash h, string->arraylist (payoff vector)
		 */

		if (depth == pnum-1){

			for(int i = 0; i < action_counts.get(depth-1); i++){
				ArrayList<ArrayList> arr1 = new ArrayList<ArrayList>(action_counts.get(depth));
				payoffs.add(arr1);
				path += Integer.toString(i);
				
				String tmp = sc.nextLine();
				
				String[] split_payoffs = tmp.split(" ");
				int k = 0;
				for(int j = 0; j < action_counts.get(depth); j++){
					
					ArrayList<Integer> arr = new ArrayList<Integer>(action_counts.get(depth));

					// loop pnum times to get payoff for each player
					for(int k2 = 0; k2 < pnum; k2++){
						arr.add(Integer.parseInt(split_payoffs[k]));
						k++;
					}
					
					path += Integer.toString(j);
					h.put(path, arr);
					path = path.substring(0, path.length()-1);
					payoffs.get(i).add(arr);
				}
				path = path.substring(0, path.length()-1);
			}
		}

		/* if we are not at the end dimension, create n arraylists in each arraylist at current dimension, with n
		 * equal to the number of current player's action count.
		 * 
		 * Then call this function n times, with depth += 1 and pass in each arraylist created.
		 * Also update the path string with the current choice, pass in to next call.
		 */
		if(depth != pnum - 1){
			for(int i = 0; i < action_counts.get(depth-1); i++){
				ArrayList<ArrayList> arr1 = new ArrayList<ArrayList>(action_counts.get(depth-1));
				payoffs.add(arr1);
			
				path += Integer.toString(i);
				construct_nD_Matrix(payoffs.get(i), pnum, action_counts, depth + 1, sc, path, h);
				path = path.substring(0, path.length()-1);
				
			}
		}

		
	}

}

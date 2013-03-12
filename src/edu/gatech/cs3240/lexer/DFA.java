package edu.gatech.cs3240.lexer;


import java.util.*;


/*
 * Creates a DFA from an existing NFA
 */

public class DFA extends NFA{
	
	public State startState;
	public ArrayList<Integer> alphabet;
	private ArrayList<State> emptyTrans;
	private ArrayList<State> currStateSet;
	private ArrayList<State> stateSet;
	private ArrayList<State> sSet;
	private ArrayList<State> transSet;
	private ArrayList<State> tSet;
	private State newState;
	private Hashtable<State, ArrayList<State>> pairs1; 
	private Hashtable<ArrayList<State>, State> pairs2;
	private boolean accept;
	private State currState;
	private boolean dead = false;
	
	
	public ArrayList<State> dfa; //DFA represented as array list of array list where nested array list holds states
	
	public DFA(NFA from){
		dfa = new ArrayList<State>();
		pairs1 = new Hashtable<State,ArrayList<State>>();
		pairs2 = new Hashtable<ArrayList<State>, State>();
		accept = false;
		create(from);
		System.out.println("*****************dfa: "+dfa);
	}
	
	
	public void create(NFA nfa){
		int count = 0;
		ArrayList<State> startStateSet = epClosure(nfa.start);
		for(State s : startStateSet){
			if(s.getAccept()){
				accept = true;
			}
		}
		State dfaStart = new State(accept);
		pairs1.put(dfaStart, startStateSet);
		pairs2.put(startStateSet, dfaStart);
		dfa.add(dfaStart);
		
		for(State s : dfa){ //check if there are any unmarked states
			if(!s.marked){
				count++;
				currState = s;
				break;
			}
		}
		
		while(count > 0){
			accept = false;
			currState.setMarked(true);
			currStateSet = pairs1.get(currState);
			
			for(int c=0;c<96;c++){
				tSet = transition(currStateSet,c);
				if(tSet.isEmpty()){ //
					sSet = tSet; //considering null states
					dead = true; // this will be a null state
				}
				else{
					sSet = epClosure(tSet);
				}
			
				if(!pairs1.containsValue(sSet)){
					for(State s : sSet){
						if(s.getAccept()){
							accept = true;
						}
					}
					newState = new State(accept);
					if(dead){
						newState.setDead(dead); // marks the state as dead if there are no transitions on character c in NFA
					}
					dfa.add(newState);
					pairs1.put(newState, sSet);
					pairs2.put(sSet, newState);
				}
				else{
					newState = pairs2.get(sSet);
				}
				
				currState.setTrans(c, newState);
			}
			
			count = 0;
			for(State s : dfa){ //check if there are any unmarked states
				if(!s.marked){
					count++;
					currState = s; //update currstate
					break;
				}
			}	
		}//end while
	}//end create

	
	/*
	 * Returns set of NFA states reachable from an NFA state on empty transitions
	 * 
	 * state is any nfa state, return DFA state made from combined NFA states
	 */
	public ArrayList<State> epClosure(State state){
		Stack<State> stack = new Stack<State>();
		emptyTrans = new ArrayList<State>();
		emptyTrans.add(state);
		for(State s : state.getTrans()[95]){
			stack.push(s); // add states to stack
			emptyTrans.add(s);
		}
		while(!stack.isEmpty()){
			State s = stack.pop();
			for(State st : s.getTrans()[95]){
				stack.push(st);
				emptyTrans.add(st);
			}
		}
		return emptyTrans;
	}

	
	/*
	 * Returns a DFA state represented as a set of NFA states reachable 
	 * from a set of states on empty transitions
	 */
	public ArrayList<State> epClosure(ArrayList<State> set){
		stateSet = new ArrayList<State>();
		for(State s : set){
			ArrayList<State> subSet = epClosure(s);

			
			for(State state : subSet){
				if(!stateSet.contains(state)){
					stateSet.add(state);
				}
			}

		}
		return stateSet;
	}
	
	/*
	 * Returns a set of states to which there is a transition on char a from an NFA state in the input set, where the 
	 * input set represents a DFA state made from combined NFA states
	 */
	public ArrayList<State> transition(ArrayList<State> set, Integer c){
		transSet = new ArrayList<State>();
		for(State s : set){
			if(!s.getTrans()[c].isEmpty()){
				for(State state : s.getTrans()[c]){
					if(!transSet.contains(state)){ //prevent adding same state twice
						transSet.add(state);
					}
				}
			}
		}
		return transSet;
	}
	
	
}
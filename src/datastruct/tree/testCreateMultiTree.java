package datastruct.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import soot.jimple.Stmt;
import soot.tagkit.Tag;
//import utilities.LoopTag;

public class testCreateMultiTree {
	public static void main(String[] args){
		
		// Creating the tree nodes
		TreeNode<String> n1 = new ArrayMultiTreeNode<>("n1");
		TreeNode<String> n2 = new ArrayMultiTreeNode<>("n2");
		TreeNode<String> n3 = new ArrayMultiTreeNode<>("n3");
		TreeNode<String> n4 = new ArrayMultiTreeNode<>("n4");
		TreeNode<String> n5 = new ArrayMultiTreeNode<>("n5");
		TreeNode<String> n6 = new ArrayMultiTreeNode<>("n6");
		TreeNode<String> n7 = new ArrayMultiTreeNode<>("n7");

		// Assigning tree nodes
		n1.add(n2);
		n1.add(n3);
		n1.add(n4);
		n2.add(n5);
		n2.add(n6);
		n4.add(n7);
		
		TraversalAction<TreeNode<String>> action = new TraversalAction<TreeNode<String>>() {
			@Override
			public void perform(TreeNode<String> node) {
				System.out.println(node.data()); // any other action goes here
			}
			
			@Override
			public boolean isCompleted() {
			    return false; // return true in order to stop traversing
			}
		};

		// Traversing pre order
		n1.traversePreOrder(action);
		System.out.println();
		n1.traversePostOrder(action);
		System.out.println();
		for (TreeNode<String> node : n1) {
			System.out.println(node.data()); // any other action goes here
		}
		
		System.out.println("testing tag func");
		testTag();
	}
	
	public static void testTag(){
		List<List<Integer>> lists = new ArrayList<List<Integer>>();
		List<Integer> list1 = new ArrayList<Integer>();
		list1.add(1);
		List<Integer> list2 = new ArrayList<Integer>();
		list2.add(1); list2.add(2);
		List<Integer> list3 = new ArrayList<Integer>();
		list3.add(1); list3.add(2); list3.add(3);
		List<Integer> list4 = new ArrayList<Integer>();
		list4.add(1); list4.add(2); list4.add(4);
		lists.add(list1); lists.add(list2); lists.add(list4); lists.add(list3);
		
		ArrayMultiTreeNode<Integer> root = new ArrayMultiTreeNode<>(1);
		
		for(List<Integer> list : lists){
			ArrayMultiTreeNode<Integer> parent = root;
			boolean foundLoopID = false;
			int previousID = 1;
			for(int id : list){
				boolean alreadyExist = false;
				if(id == 1) {
					foundLoopID = true;
				} else if(id != 1 && foundLoopID == true){
					ArrayMultiTreeNode<Integer> child = new ArrayMultiTreeNode<>(id);					
					for (TreeNode<Integer> subroot : root) {
						if(subroot.data() == previousID){
							parent = (ArrayMultiTreeNode<Integer>) subroot; 
						} else if(subroot.data() == id){
							alreadyExist = true;
							break;
						}
					}
					if(alreadyExist == false){
						parent.add(child);
						parent = child;
					}
				}
				previousID = id;
			}
		}
		
		
		System.out.println("-------testing for loop--------");
		int index = 0;
		for (TreeNode<Integer> node : root) {
			//System.out.println(node.data() + ":" +node.level()+":"+node.height()); // any other action goes here
			System.out.println("node " + node.data() + "'s subtree is:");
			for(TreeNode<Integer> node1 : node){
				System.out.println(node1.data() + ":" +node1.level()+":"+node1.height());
			}
		}
		
		
		List<TreeNode<Integer>> allLeaves = new ArrayList<TreeNode<Integer>>();
		for (TreeNode<Integer> node : root) {
			if(node.isLeaf()){
				allLeaves.add(node);
			}
		}
		
		for(TreeNode<Integer> leaf : allLeaves){
			System.out.println("leaf node is: " + leaf.data());
			Collection<? extends TreeNode<Integer>> path = root.path(leaf);
			System.out.print("the path to this leaf node is: ");
			for(TreeNode<Integer> pathnode : path){
				System.out.print(pathnode.data() + ",");
			}
			System.out.println();
		}
		
		
//		System.out.println("-------testing while loop--------");
//		// Iterating over the tree elements using Iterator
//		Iterator<TreeNode<Integer>> iterator = root.iterator();
//		while (iterator.hasNext()) {
//			TreeNode<Integer> node = iterator.next();
//			System.out.println(node.data());
//		}
		int MaxHeight = 0;
		// Iterating over the tree elements using Iterator
		Iterator<TreeNode<Integer>> iterator = root.iterator();
		while (iterator.hasNext()) {
			TreeNode<Integer> node = iterator.next();
			MaxHeight = (MaxHeight > node.height()) ? MaxHeight : node.height(); 
		}
		for(int i = MaxHeight; i >= 0; i--){
			iterator = root.iterator();
			while (iterator.hasNext()) {
				TreeNode<Integer> node = iterator.next();
				if(node.height() == i){
					System.out.print(node.data() + " ");
				}
			}
			System.out.println();
		}
	}
}

package Utilities;

import java.util.ArrayList;
import java.util.List;

public class Mergesort {

    public static List<AlphaBetaTreeNode> mergesort(List<AlphaBetaTreeNode> list) {
        if(list.size() > 1) {
            List<List<AlphaBetaTreeNode>> lists = partition(list);
            List<AlphaBetaTreeNode> list1 = lists.get(0);
            List<AlphaBetaTreeNode> list2 = lists.get(1);

            list1 = mergesort(list1);
            list2 = mergesort(list2);

            return merge(list1, list2);
        }
        else {
            return list;
        }
    }

    private static List<List<AlphaBetaTreeNode>> partition(List<AlphaBetaTreeNode> list) {
        List<List<AlphaBetaTreeNode>> lists = new ArrayList<>();
        List<AlphaBetaTreeNode> list1 = new ArrayList<>();
        List<AlphaBetaTreeNode> list2 = new ArrayList<>();

        int middle = list.size() / 2;
        for(int i = 0; i < list.size(); i++) {
            if(i < middle) {
                list1.add(list.get(i));
            }
            else {
                list2.add(list.get(i));
            }
        }

        lists.add(list1);
        lists.add(list2);

        return lists;
    }

    private static List<AlphaBetaTreeNode> merge(List<AlphaBetaTreeNode> list1, List<AlphaBetaTreeNode> list2) {
        List<AlphaBetaTreeNode> list = new ArrayList<>();
        int index1 = 0;
        int index2 = 0;

        while(index1 < list1.size() && index2 < list2.size()) {
            if(list1.get(index1).getScore() <= list2.get(index2).getScore()) {
                list.add(list1.get(index1));
                index1++;
            }
            else {
                list.add(list2.get(index2));
                index2++;
            }
        }

        while(index1 < list1.size()) {
            list.add(list1.get(index1));
            index1++;
        }

        while(index2 < list2.size()) {
            list.add(list2.get(index2));
            index2++;
        }

        return list;
    }

}

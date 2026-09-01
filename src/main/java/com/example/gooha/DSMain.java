package com.example.gooha;

import java.util.*;

public class DSMain {
    public static void main(String[] args) {
        // int[] arr1 = new int[4];
        // int[] arr2 = {10, 20, 30};
        // String[] arr3 = new String[4];

        // 배열 확장
        int[] arr = {10, 20, 30};
        int[] arr2 = Arrays.copyOf(arr, 10);
        arr2[9] = 1000;
        for(int v : arr2) {
            System.out.println(v);
        }

        // 동적 배열
        ArrayList<String> names = new ArrayList<>(1000);
        names.add("둘리");
        System.out.println(names.size()); // => 1

        // Arrays.sort();

        HashMap<String, ArrayList<String>> d = new HashMap<>();

        // 인접행렬, 2차원배열
        int[][] graph1 = {
                new int[]{0, 1, 1, 0, 0, 0},
                new int[]{1, 0, 1, 1, 0, 0},
                new int[]{1, 1, 0, 1, 1, 0},
                new int[]{0, 1, 1, 0, 1, 1},
                new int[]{0, 0, 1, 1, 0, 0},
                new int[]{0, 0, 0, 1, 0, 0},
        };

        // 인접리스트
        HashMap<Integer, List<Integer>> graph2 = new HashMap<>();
        graph2.put(1, Arrays.asList(2, 3));
        graph2.put(2, Arrays.asList(1, 3, 4));
        graph2.put(3, Arrays.asList(1, 2, 4, 5));
        graph2.put(4, Arrays.asList(2, 3, 5, 6));
        graph2.put(5, Arrays.asList(3, 4));
        graph2.put(6, Arrays.asList(4));

        // 탐색할 대상
        Stack<Integer> stack = new Stack<>();
        stack.add(1);
        boolean[] visited = new boolean[7];
        visited[1] = true;

        System.out.println("start");
        while(stack.size() > 0) {
            // 1. 현재 탐색 대상
            // System.out.println("탐색중...");
            int v = stack.pop();
            // System.out.println("탐색대상" + v);
            System.out.println(v);

            // 2, 다음 탐색 대상
            List<Integer> items = graph2.get(v);
            for(int item : items) {
                if(visited[item]) {
                   continue;
                }
                // System.out.println("다음탐색대상" + item);
                stack.add(item);
                visited[item] = true;
            }
        }


        System.out.println("start2");
        // BFS
        int[][] graph33 = {
                new int[]{0, 1, 1, 0, 0, 0},
                new int[]{1, 0, 1, 1, 0, 0},
                new int[]{1, 1, 0, 1, 1, 0},
                new int[]{0, 1, 1, 0, 1, 1},
                new int[]{0, 0, 1, 1, 0, 0},
                new int[]{0, 0, 0, 1, 0, 0},
        };

        Queue<Integer> queue33 = new LinkedList<>();
        boolean[] visited33 = new boolean[graph33.length];

        queue33.offer(0);
        visited33[0] = true;

        while(queue33.size() > 0) {
            int i = queue33.poll();

            System.out.println(i);

            for(int j = 0; j < graph33[0].length; j++) {
                if(graph33[i][j] == 1 && !visited33[j]) {
                    queue33.offer(j);
                    visited33[j] = true;
                }
            }
        }
    }
}

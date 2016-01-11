package com.java.myapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

public class CX600_X2_M8 {

    public static void main(String[] args) {
        Dialog.setLAF();
        Selectfile Select = new Selectfile();
        int index = -1;
        File[] files = Select.getFile().listFiles();
        Wait wait = new Wait();
        try {
            if (Select.getChooser().getSelectedFile().getName().contains(".txt")) {
                if (Select.getChooser().getSelectedFile().getName().contains("description.txt")) {
                    BufferedReader br = new BufferedReader(new FileReader(Select.getFile()));
                    String pathOutput = Select.getFile().getPath();
                    new File(Select.getChooser().getCurrentDirectory() + "\\Total_description.csv").delete();

                    Sub(br, pathOutput, Select.getChooser().getCurrentDirectory());
                    wait.dispose();
                    Dialog.Success();
                    System.exit(0);
                } else {
                    System.exit(0);
                }
            } else {
                new File(Select.getChooser().getSelectedFile() + "\\Total_description.csv").delete();
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.compare(f1.lastModified(), f2.lastModified());
                    }
                });
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().contains("description.txt")) {
                        index = i;
                        BufferedReader br = new BufferedReader(new FileReader(files[i]));
                        String pathOutput = files[i].getPath();
                        Sub(br, pathOutput, Select.getChooser().getSelectedFile());
                    }
                }
                wait.dispose();
                Dialog.Success();
                System.exit(0);
            }
        } catch (NullPointerException ex) {
            System.exit(0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (Select.getChooser().getSelectedFile().getName().contains("description.txt")) {
                Dialog.FileError(Select.getFile());
            } else {
                Dialog.FileError(files[index].getName());
            }
        }
    }

    private static void Sub(BufferedReader br, String path, File Directory) throws IOException {
        String line;
        String all = "";
        String[] w = new String[10000];

        int c = 0;
        int[][] total_slot = new int[20][200];
        int Total_Port = 0;
        int Total_Free_Port = 0;
        int Total_Free_Port_1G = 0;
        int Total_Free_Port_10G = 0;
        int main = 0;
        int slot = 0;
        int max_main = 0;
        int max_slot = 0;
        while ((line = br.readLine()) != null) {
            String[] arr = line.split("/");
            String[] arr2 = line.split(" ");
            if (arr.length > 1 && (arr[0].contains("GE") && !line.contains("GE0/0/0"))) {

                if (arr[2].charAt(0) != '.' && arr[2].charAt(1) != '.' && arr[2].charAt(2) != '.' && arr[2].charAt(3) != '.' && arr[2].charAt(4) != '.' && arr[2].charAt(5) != '.') {
                    Total_Port++;
                    String str = arr2[0] + "," + line.substring(30, 35) + "," + line.substring(38, 43) + "," + line.substring(47);
                    String[] arr3 = str.split(",");
                    if (arr3[3].charAt(0) == ' ') {
                        arr3[3] = "false";
                    }

                    String s = arr3[0] + "," + arr3[1] + "," + arr3[2] + "," + arr3[3];
                    if (s.contains("false")) {
                        String[] a = s.split("/");
                        w[c] = a[0].substring(2) + "/" + a[1] + "/" + a[2].substring(0, a[2].indexOf(','));
                        if (w[c].contains("10G")) {
                            Total_Free_Port_10G++;
                        } else {
                            Total_Free_Port_1G++;
                        }
                        String[] b = w[c].split("/");
                        main = Integer.valueOf(b[0]);
                        slot = Integer.valueOf(b[1]);
                        max_main = max_main < main ? main : max_main;
                        max_slot = max_slot < slot ? slot : max_slot;
                        total_slot[main][slot]++;
                        c++;
                    } else if (arr3[3].indexOf("HUAWEI") == 0) {
                        String[] a = s.split("/");
                        w[c] = a[0].substring(2) + "/" + a[1] + "/" + a[2].substring(0, a[2].indexOf(','));
                        String[] b = w[c].split("/");
                        main = Integer.valueOf(b[0]);
                        slot = Integer.valueOf(b[1]);
                        max_main = max_main < main ? main : max_main;
                        max_slot = max_slot < slot ? slot : max_slot;
                        total_slot[main][slot]++;
                        c++;
                    }
                }

            }
        }
        br.close();

        Total_Free_Port = Total_Free_Port_10G + Total_Free_Port_1G;
        String[] PATH = path.split("\\.");
        String p = PATH[0].substring(PATH[0].lastIndexOf('\\') + 1) + "." + PATH[1] + "." + PATH[2] + "." + PATH[3].substring(0, PATH[3].indexOf('-'));
        String Loopback = "";
        String Str = "";
        Loopback = "Loopback : " + p + "\n" + "Total_Port = "
                + Total_Port + "\n" + "Total_Free_Port = "
                + Total_Free_Port + "\n" + "Total_Free_Port_10G = "
                + Total_Free_Port_10G + "\n" + "Total_Free_Port_1G = "
                + Total_Free_Port_1G;
        for (int i = 0; i <= max_main; i++) {
            for (int j = 0; j <= max_slot; j++) {
                if (total_slot[i][j] != 0) {
                    Str += "\n" + String.format("Card[%d] Slot[%d] Free = ", i, j) + total_slot[i][j];
                }
            }
        }
        all = Loopback + Str + "\n\n";
        System.out.println(all);
        FileWriter CardAllocation;
        CardAllocation = new FileWriter(Directory + "\\Total_description.csv", true);
        Writer.CardAllocation(CardAllocation, all);
    }

}

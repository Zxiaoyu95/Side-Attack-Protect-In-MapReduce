package Test;

public class addCount {
        public String countAndSay(int n) {
            if(n == 1)return "1";
            String result = "";
            String tmp = countAndSay(n-1);
            int count = 1;
            int len = tmp.length();
            for(int i = 0;i < len;i++){
                if(i != len-1) {
                    if (tmp.charAt(i) != tmp.charAt(i + 1)) {
                        result += String.valueOf(count) + tmp.charAt(i);
                        count = 1;
                        continue;
                    } else if (tmp.charAt(i) == tmp.charAt(i + 1)) {
                        count++;
                    }
                }
                else{result += String.valueOf(count) + tmp.charAt(i);}
            }
            return result;
        }
        public static void main(String[] args){
            addCount ac = new addCount();
            System.out.println(ac.countAndSay(5));
        }
    }


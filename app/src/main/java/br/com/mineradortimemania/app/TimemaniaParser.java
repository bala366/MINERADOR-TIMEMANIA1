package br.com.mineradortimemania.app;
import java.io.*;import java.util.*;import java.util.regex.*;
public final class TimemaniaParser{
 public static final class Draw{public final int contest;public final int[]n;public Draw(int c,int[]a){contest=c;n=a;}}
 public static ArrayList<Draw>parse(InputStream in)throws IOException{ArrayList<Draw>o=new ArrayList<>();BufferedReader b=new BufferedReader(new InputStreamReader(in));String l;int f=1;while((l=b.readLine())!=null){Matcher m=Pattern.compile("\\d+").matcher(l);ArrayList<Integer>v=new ArrayList<>();while(m.find())v.add(Integer.parseInt(m.group()));if(v.size()<7)continue;int[]a=new int[7];for(int i=0;i<7;i++)a[i]=v.get(v.size()-7+i);TreeSet<Integer>s=new TreeSet<>();boolean ok=true;for(int x:a)if(x<1||x>80||!s.add(x)){ok=false;break;}if(ok){Arrays.sort(a);o.add(new Draw(v.size()>7?v.get(0):f++,a));}}if(o.size()<10)throw new IOException("Base inválida.");return o;}
}
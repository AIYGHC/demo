package com.example.demo.csv;

import com.example.demo.util.DBUtil;
import com.example.demo.util.XCell;
import com.example.demo.util.XRow;

import java.io.*;
import java.util.*;

public class ReadCSV {
	/**
	 * 获取第一行的数据,作为数据库字段的注释

	 * @return
	 * @throws Exception
	 */
	public static XRow getFirstRowData(String filePathName) throws Exception {
		File file =new File(filePathName);
		if (!file.exists()) {
			return null;
		}
		//这里要统一编码
		InputStreamReader read=new InputStreamReader(new FileInputStream(file),"GBK");
		BufferedReader bfr=new BufferedReader(read);
		String line = "";
		//第一行的数据封装
		XRow firstRow = new XRow();
		List<XCell> cells = new ArrayList<>();
		Set<String> strings=new HashSet<>();
		while ((line = bfr.readLine()) != null) {
			System.out.println(line);
			String[] strs=cvsField(line);
			for (String s:strs){
				System.out.println(s);
			}

			int g=0;
			for (String s:strs){
				XCell xCell = new XCell();
				xCell.setCellIndex(g+1);
				xCell.setValue("["+s+"]");
				if (!strings.add(s)){//重复字段
					xCell.setValue("["+s+(g+1)+"]");
				}
				cells.add(xCell);
				g++;
			}
			break;
		}
		bfr.close();
		firstRow.setRowIndex(1);
		firstRow.setRowValue(cells);
		//遍历查看

			List<XCell> rowValue = firstRow.getRowValue();
			System.out.println("======rowIndex===");
			for (XCell xCell : rowValue) {
				System.out.println(xCell);
			}

		return firstRow;
	}
	private static String[] cvsField(String line){
		List<String> fields = new LinkedList<>();
		char[] alpah = line.toCharArray();
		boolean isFieldStart = true;
		int pos = 0; int len = 0; boolean yinhao = false;
		int i=0;
		boolean charStart=false;
		boolean charEnd=false;
		for(char c : alpah){
			if(isFieldStart){
				len = 0;
				isFieldStart = false;
			}
			if(c == '\"'){
				yinhao = !yinhao;
			}
			if(c == '\"'&&i==0){
				charStart=true;
			}
			if(c == '\"'&&i==alpah.length-1){
				charEnd=true;
			}
			if(c == ',' && !yinhao){
				fields.add(new String(alpah, pos - len, len));
				isFieldStart = true;
			}
			pos++; len++;i++;
		}
		if (charStart&&charEnd){
			List<String> field = new LinkedList<>();
			for (String s:fields){
				//双引号去除
				String str=s.substring(1,s.length()-1);
				field.add(str);
			}
			return field.toArray(new String[0]);
		}
		return fields.toArray(new String[0]);
	}
	/**
	 * 以第一行为宽度获取其余的数据
	 * @param firstRow
	 * @return
	 * @throws Exception
	 */
	public static List<XRow> getOtherData(String filePathName,XRow firstRow,List<String> columnList,String tableName,int l) throws Exception {

		//所有的行数据
		List<XRow> rows = new ArrayList<>();

		File file =new File(filePathName);
		if (!file.exists()) {
			return null;
		}

		try {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(filePathName)));
			BufferedReader in = new BufferedReader(new InputStreamReader(bis, "GBK"), 10 * 1024 * 1024);//10M缓存
			//第一行的列数
			int columnSize = firstRow.getRowValue().size();
			String line = "";
//			System.out.println(columnSize);
			int j=0;
			while (in.ready()) {
				//跳过多少行
				if (j<=l*50000){
					j++;
					continue;
				}
				line = in.readLine();
				List<XCell> cells = new ArrayList<>();
				//去掉第一行
				if (j<=0) {
					j++;
					continue;
				}
				XRow xRow = new XRow();
				xRow.setRowIndex(j+1);
				String[] strs=cvsField(line);
				int g=1;
				for (int i=0;i<columnSize;i++){
					XCell xCell = new XCell();
					xCell.setCellIndex(g);
					xCell.setValue(strs[i]);
					cells.add(xCell);
					g++;
				}
				j++;
				xRow.setRowValue(cells);
				rows.add(xRow);
				if(j%5000==0){
					int[] ints = DBUtil.insertRowBatch(columnList, rows,tableName);
					rows = new ArrayList<>();
				}
			if (j/50000==0){
				break;
			}
			}
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}



////		//遍历查看
//		for (XRow row : rows) {
//			int rowIndex = row.getRowIndex();
//			List<XCell> rowValue = row.getRowValue();
//			System.out.println("======rowIndex==="+rowIndex);
//			System.out.println("_________________________"+rowValue.size());
//			for (XCell xCell : rowValue) {
//				System.out.println(xCell);
//			}
//		}
		return rows;
	}
}

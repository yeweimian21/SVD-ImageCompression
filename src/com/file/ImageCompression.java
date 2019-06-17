
package com.file;

import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class ImageCompression {

	static int height = 0, width = 0, max_pixel_value;
	public static String fileName;
	public static ArrayList<ArrayList<Integer>> pixelV;

	public static void main(String ar[]) throws IOException {
		
		String directory="picture3/";
		String pgmFile=directory+"pic3.pgm";
		String pgm_b_File=directory+"pic3_b.pgm";
		
		
		String headerFile=directory+"header.txt";
		String SVDFile=directory+"SVD.txt";
		String rank="10";
		
		String sourceFilename=directory+"image_b.pgm.SVD";
		
		//�õ�pgm�Ķ������ļ�����������SVD.txt�ļ�
		step1(pgmFile,SVDFile);
		
		//��pgm�Ķ������ļ�ת��ΪASCII�ļ�
		step2(pgm_b_File);
		
		//����header.txt,SVD.txt,rankֵ����ͼ�����ѹ���洢
		step3(headerFile, SVDFile, rank, sourceFilename);
		
		//��ѹ��֮���.pgm.SVD�ļ����н��룬���Բ鿴�ָ�����ͼ��Ч��
		step4(sourceFilename);
	}
	
	//����pgm�Ķ������ļ��ʹ洢�����SVD.txt�ļ�
	public static void step1(String fileName,String SVDFile) throws IOException {
		
		pixelV = new ArrayList<ArrayList<Integer>>();
		File fp = new File(fileName);
		Scanner sc = new Scanner(fp);
		sc.nextLine();
		sc.nextLine();
		width = Integer.parseInt(sc.next());
		System.out.println(width);
		height = Integer.parseInt(sc.next());
		System.out.println(height);
		max_pixel_value = Integer.parseInt(sc.next());
		System.out.println(max_pixel_value);
		//�������ȡ��pixelLV��
		for (int i1 = 0; i1 < height; i1++) {
			ArrayList<Integer> t = new ArrayList<Integer>();
			for (int j1 = 0; j1 < width; j1++) {
				t.add(Integer.parseInt(sc.next()));
			}
			pixelV.add(t);
		}
		DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
				new FileOutputStream(fileName.substring(0, fileName.indexOf(".")) + "_b" + ".pgm")));
		//��width��height��int-->String
		String width_binary = binary_2_convert(width);
		String height_binary = binary_2_convert(height);
		//b1,b2,h1,h2�ֱ��width��height��ǰ8bit�ͺ�8bit
		int b1 = integer_value(width_binary.substring(0, 8));
		int b2 = integer_value(width_binary.substring(8, 16));
		int h1 = integer_value(height_binary.substring(0, 8));
		int h2 = integer_value(height_binary.substring(8, 16));
		//�Զ�������ʽд��b1,b2,h1,h2,max_pixel_value
		dos.writeByte(b1);
		dos.writeByte(b2);
		dos.writeByte(h1);
		dos.writeByte(h2);
		dos.writeByte(max_pixel_value);
		
		//�Զ�������ʽд������pixelV
		for (ArrayList<Integer> a1 : pixelV) {
			for (int i = 0; i < a1.size(); i++) {
				dos.writeByte(a1.get(i));
			}
		}
		/// System.out.println("inside a1"+ a[1]);
		//����SVD�����ļ�
		createSVD(fileName,SVDFile);
		dos.flush();
		dos.close();
		sc.close();
	}
	
	//��ȡpgm�Ķ������ļ���Ȼ��ת��Ϊint�Ͳ������ļ���
	public static void step2(String fileName) throws IOException {
		
		DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));
		pixelV = new ArrayList<ArrayList<Integer>>();
		try {
			PrintWriter pw = new PrintWriter(fileName.substring(0, fileName.indexOf(".")) + "_2" + ".pgm");
			pw.println("P2");
			pw.println("# Created by IrfanView");
			byte hw[] = new byte[5];
			dis.read(hw);
			
			//��width,height��byte-->int-->String-->int
			width = integer_value(binary_1_convert(byte2Int(hw[0])) + binary_1_convert(byte2Int(hw[1])));
			height = integer_value(binary_1_convert(byte2Int(hw[2])) + binary_1_convert(byte2Int(hw[3])));

			max_pixel_value = byte2Int(hw[4]);
			// System.out.println(width+" "+height+" "+max_pixel_value);
			pw.write(width + " ");
			pw.write(height + "\n");
			pw.write(max_pixel_value + "\n");
			byte t[] = new byte[width * height];
			//���������ļ�����byte����t��
			dis.read(t);
			int tij = 0;
			//��byte����t�е�����ת��Ϊint�ͣ�Ȼ������ļ���
			for (int ti = 0; ti < height; ti++) {
				for (int tj = 0; tj < width; tj++) {
					int pix;
					if ((new Byte(t[tij])).intValue() < 0) {
						pix = 128 + (127 + (new Byte(t[tij])).intValue() + 1);
					} else {
						pix = (new Byte(t[tij])).intValue();
					}
					pw.write(pix + " ");
					tij++;
				}
				pw.write("\n");
			}
			pw.close();
		} catch (FileNotFoundException e) {
			System.out.println("Cannot create op file");
		}
		dis.close();
	}
	
	//����header.txt,SVD.txt,rankֵ������ѹ��֮����ļ�
	public static void step3(String headerFile,String SVDFile,String rank,String sourceFile) {
		
		SVD Svd2 = new SVD();
		Svd2.readHeader(headerFile);
		Svd2.readText(SVDFile);
		Svd2.setRank(Integer.parseInt(rank));
		// System.out.println("Relative error: " + Svd2.getRelativeError());
		Svd2.writeBinary(sourceFile);
		
	}
	
	//����.pgm.SVDѹ���ļ���������ԭͼ���бȽ�
	public static void step4(String fileName) {
		
		String sourceFilename = fileName;
		SVD mySvd2 = new SVD();
		mySvd2.readBinary(sourceFilename);
		//��ѹ���ļ�.pgm.SVD�ļ��ж�ȡ�õ�u,s,v����
		Matrix u = mySvd2.getU();
		Matrix s = mySvd2.getS();
		Matrix v = mySvd2.getV();
		Matrix restore = u.times(s.times(v.transpose()));
		if (mySvd2.isTransposed()) 
		{
			restore = restore.transpose();
		}
		//��ȡ�Ҷ�ֵ
		int[][] gray_values = new int[restore.getRowDimension()][restore.getColumnDimension()];
		int n = 0;
		for (int i = 0; i < gray_values.length; i++) {
			for (int j = 0; j < gray_values[i].length; j++) {
				n = (int) restore.get(i, j);
				if (n < 0) {
					gray_values[i][j] = 0;
				} else if (n > 255) {
					gray_values[i][j] = 255;
				} else {
					gray_values[i][j] = n;
				}
			}
		}
		//���ɽ���֮���pgmͼ���ļ�
		Pgm pm1 = new Pgm();
		pm1.setHeight(gray_values.length);
		pm1.setWidth(gray_values[0].length);
		pm1.setMaxval(255);
		pm1.setGrays(gray_values);
		String fn = sourceFilename.split("_b\\.pgm\\.")[0];
		String tragetFilename = fn + "_k.pgm";
		pm1.writePgm(tragetFilename);
		
	}

	public static void createSVD(String src,String createSVDFile) {
		Pgm pm = new Pgm();
		pm.readPgm(src);
		int[][] gray_values = pm.getGrays();

		//����ԭʼͼ���Ӧ�ľ���m
		Matrix m = new Matrix(pm.getHeight(), pm.getWidth());
		for (int i = 0; i < pm.getHeight(); i++) {
			for (int j = 0; j < pm.getWidth(); j++) {
				m.set(i, j, (double) gray_values[i][j]);
			}
		}
		if (pm.getWidth() > pm.getHeight()) {
			m = m.transpose();
		}

		//Jama����svd()�������ɾ���m��SVD����
		SingularValueDecomposition svd = m.svd();
		SVD Svd2;
		Svd2 = new SVD();
		//��svd2�ֱ���ԭʼͼ���svd�����u,s,v����
		Svd2.setU(svd.getU());
		Svd2.setS(svd.getS());
		Svd2.setV(svd.getV());
		//��svd2д��SVD.txt����ð���u,s,v���������SVD.txt�ļ�
		Svd2.writeText(createSVDFile);

	}

	//��int-->String
	public static String binary_1_convert(int a) {
		String x = Integer.toBinaryString(a);
		//���ת������ַ������Ȳ���8������Ӧ����ֵ����8bit1���ֽڣ�ǰ��0
		if (x.length() <= 8) {
			for (int i = x.length(); i < 8; i++) {
				x = "0" + x;
			}
		}
		return x;
	}

	//��int-->String
	public static String binary_2_convert(int a) {
		String x = Integer.toBinaryString(a);
		//���ת������ַ������Ȳ���16������Ӧ����ֵ����16bit2���ֽڣ�ǰ��0
		if (x.length() <= 16) {
			for (int i = x.length(); i < 16; i++) {
				x = "0" + x;
			}
		}
		return x;
	}

	//String-->int
	public static int integer_value(String b) {
		int i = 0;
		StringBuffer b1 = new StringBuffer(b);
		b1.reverse();
		// System.out.println(b1);
		for (int i1 = 0; i1 < b1.length(); i1++) {
			if (b1.charAt(i1) == '1') {
				i += Math.pow(2, i1);
			}
		}
		return i;
	}

	//byte-->int
	public static int byte2Int(Byte b) {
		if (b.intValue() < 0) {
			return 128 + (127 + b.intValue() + 1);
		} else {
			return b.intValue();
		}
	}

}

//����SVD�������
class SVD {
	
	//��Ӧ��u,s,v��������
	private Matrix u;
	private Matrix s;
	private Matrix v;
	//rankֵ
	private int rank;
	
	private boolean transposed;
	private final double FACTOR = 32768.0;
	
	//��u,s,v���������SVD.txt�ļ��ж�����
	public void readText(String fn) {
		try {
			Scanner scanner = new Scanner(new File(fn));
			for (int i = 0; i < u.getRowDimension(); i++) {
				for (int j = 0; j < u.getColumnDimension(); j++) {
					u.set(i, j, scanner.nextDouble());
				}
			}
			for (int i = 0; i < s.getRowDimension(); i++) {
				for (int j = 0; j < s.getColumnDimension(); j++) {
					s.set(i, j, scanner.nextDouble());
				}
			}
			for (int i = 0; i < v.getRowDimension(); i++) {
				for (int j = 0; j < v.getColumnDimension(); j++) {
					v.set(i, j, scanner.nextDouble());
				}
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write three matrices to a text SVD file.
	 * 
	 * @param fn
	 *            fn of the SVD file.
	 */
	//��u,s,v��������д��SVD.txt�ļ���
	public void writeText(String fn) {
		try {
			PrintWriter op = new PrintWriter(fn);
			for (int i = 0; i < u.getRowDimension(); i++) {
				for (int j = 0; j < v.getRowDimension(); j++) {
					op.print(u.get(i, j) + " ");
				}
			}
			for (int i = 0; i < v.getRowDimension(); i++) {
				for (int j = 0; j < v.getRowDimension(); j++) {
					op.print(s.get(i, j) + " ");
				}
			}
			for (int i = 0; i < v.getRowDimension(); i++) {
				for (int j = 0; j < v.getRowDimension(); j++) {
					op.print(v.get(i, j) + " ");
				}
			}
			op.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//��ȡѹ���ļ�.pgm.SVD�ļ�
	public void readBinary(String fn) {
		try {
			DataInputStream ip = new DataInputStream(new FileInputStream(fn));
			if (ip.readBoolean() == true) {
				transposed = true;
			} else {
				transposed = false;
			}
			//��ȡm,n,rank��ֵ
			int urd = ip.readShort(); // row
			int vrd = ip.readShort(); // column
			rank = ip.readShort(); // rank
			//�ֱ��ȡu,s,v����
			u = new Matrix(urd, rank);
			s = new Matrix(rank, rank);
			v = new Matrix(vrd, rank);
			for (int i = 0; i < urd; i++) {
				for (int j = 0; j < rank; j++) {
					u.set(i, j, ip.readShort() / FACTOR);
				}
			}

			for (int i = 0; i < rank; i++) {
				for (int j = 0; j < rank; j++) {
					if (i == j) {
						s.set(i, j, ip.readFloat());
					} else {
						s.set(i, j, 0.0);
					}
				}
			}
			for (int i = 0; i < vrd; i++) {
				for (int j = 0; j < rank; j++) {
					v.set(i, j, ip.readShort() / FACTOR);
				}
			}
			ip.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//����height,width,SVD����rankֵ��������ѹ��֮���.pgm.SVD�ļ�
	public void writeBinary(String fn) {
		try {
			DataOutputStream op = new DataOutputStream(new FileOutputStream(fn));
			if (transposed) {
				op.writeBoolean(true);
			} else {
				op.writeBoolean(false);
			}
			op.writeShort(u.getRowDimension()); // height
			op.writeShort(v.getColumnDimension()); // width
			op.writeShort(rank); // rank
			for (int i = 0; i < u.getRowDimension(); i++) {
				for (int j = 0; j < rank; j++) {
					op.writeShort((short) (u.get(i, j) * FACTOR));
				}
			}
			for (int i = 0; i < rank; i++) {
				op.writeFloat((float) (s.get(i, i)));
			}
			for (int i = 0; i < v.getRowDimension(); i++) {
				for (int j = 0; j < rank; j++) {
					op.writeShort((short) (v.get(i, j) * FACTOR));
				}
			}
			op.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//��ȡheader.txt�ļ����Ӷ�ȷ������m,nֵ
	public void readHeader(String fn) {
		try {
			Scanner scanner = new Scanner(new File(fn));
			int width = scanner.nextInt();
			int height = scanner.nextInt();
			if (width > height) {
				transposed = true;
				u = new Matrix(width, height);
				s = new Matrix(height, height);
				v = new Matrix(height, height);
			} else {
				transposed = false;
				u = new Matrix(height, width);
				s = new Matrix(width, width);
				v = new Matrix(width, width);
			}

			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public double getRelativeError() {
		int size = Math.min(u.getRowDimension(), v.getRowDimension());

		double norm1 = 0;
		for (int i = 0; i < size; i++) {
			norm1 += s.get(i, i) * s.get(i, i);
		}

		double norm2 = 0;
		for (int i = rank; i < size; i++) {
			norm2 += s.get(i, i) * s.get(i, i);
		}
		return Math.sqrt(norm2 / norm1);
	}

	//get(),set()����
	public Matrix getU() {
		return u;
	}

	public void setU(Matrix u) {
		this.u = u;
	}

	public Matrix getS() {
		return s;
	}

	public void setS(Matrix s) {
		this.s = s;
	}

	public Matrix getV() {
		return v;
	}

	public void setV(Matrix v) {
		this.v = v;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public boolean isTransposed() {
		return transposed;
	}

	public void setTransposed(boolean transposed) {
		this.transposed = transposed;
	}

}

//����pgm�ļ�����
class Pgm {

	//���ԣ����ߣ��Ҷ�ֵ���Ҷ�ֵ�����ֵ
	static int width, height, gray_values[][], maxval;

	public static void main1() {

	}

	//get(),set()����
	public int[][] getGrays() {
		return gray_values;
	}

	public int getHeight() {
		return height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setMaxval(int maxval) {
		this.maxval = maxval;
	}

	public void setGrays(int[][] gray_values) {
		this.gray_values = gray_values;
	}

	public int getWidth() {
		return width;
	}

	//��ASCII��ʽд������pgm�ļ�
	public void writePgm(String fn) {
		try {
			PrintWriter op = new PrintWriter(fn);
			op.println("P2");
			op.println(width + " " + height);
			op.println(maxval);

			int i = 0;
			while (i < height * width) {
				op.print(gray_values[i / width][i % width] + " ");
				if ((i + 1) % 16 == 0) {
					op.println();
				}
				i++;
			}
			op.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//��ȡpgm��ASCII��ʽ�ļ�
	public void readPgm(String fn) {
		try {
			Scanner scanner = new Scanner(new File(fn));
			scanner.nextLine();
			scanner.nextLine();

			width = Integer.parseInt(scanner.next());
			height = Integer.parseInt(scanner.next());
			// line = scanner.nextLine().trim();
			maxval = Integer.parseInt(scanner.next());
			// System.out.println("wid="+width+"hie="+height);
			gray_values = new int[height][width];

			int i = 0;
			while (scanner.hasNextInt()) {
				gray_values[i / width][i % width] = scanner.nextInt();
				i++;
			}
			scanner.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

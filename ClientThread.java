import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class ClientThread extends Thread {

	private Socket socket;
	private int user_id;
	private double RTT;
	private int N;
	private int repet;

	public ClientThread(Socket socket, int user_id,int N,int repet) {
		this.socket = socket;
		this.user_id = user_id;
		this.N=N;
		this.repet=repet;
	}
	
	public void write_RTT(double RTT,int N) {
		BufferedWriter bw = null;
		FileWriter fw = null;
		try {
			File file = new File("RTT.txt");

			// it creates the file if the file is not already present
			if (!file.exists()) {
				file.createNewFile();
			}

			// appends to the file
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			bw.write(RTT+"\t"+N+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();

			}
		}

	}
	
	public void run() {
		int max_requests = 300;
		long sumRTT=0;
		double avg_throughput_user=0.0;
		
		try {
			String socket_address = socket.getRemoteSocketAddress().toString();
			for (int i = 1; i <= max_requests; i++) {
				long startTime=System.nanoTime();
				OutputStream output = socket.getOutputStream();
				PrintWriter writer = new PrintWriter(output, true);
				String text = "HELLO";
				writer.println(text);
				writer.println(user_id);
				writer.println(socket_address);
				InputStream input = socket.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				String response = reader.readLine();
				System.out.println(response);
				byte[] payload = reader.readLine().getBytes();
				long RTT=System.nanoTime()-startTime;
				sumRTT+=RTT;
				if(i==max_requests){
					avg_throughput_user=Double.parseDouble(reader.readLine());
				}
			}
			RTT=sumRTT;
			Client.RTT.add(RTT);
			socket.close();
		} catch (IOException ex) {
			System.out.println("Client exception: " + ex.getMessage());
			ex.printStackTrace();
		}
		
		if(Client.RTT.size()==N){
			long sum=0;
		for(int i=0;i<Client.RTT.size();i++){
			sum+=Client.RTT.get(i);
		}
		double avg=sum/N;
		Client.repetitions.add(avg);
		Client.RTT.clear();
		}
		
		if(Client.repetitions.size()==repet){
			long sumRep=0;
			for(int i=0;i<Client.repetitions.size();i++){
				sumRep+=Client.repetitions.get(i);
			}
			double avgRep=sumRep/repet;
			write_RTT(avgRep,N);
			Client.repetitions.clear();
		}
		
	}
}
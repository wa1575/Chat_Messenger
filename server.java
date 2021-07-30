package messenger;

import java.io.*;
import java.net.*;
import java.util.*;


class ClientData{
	public String nickname = new String(); //닉네임(클라이언트에서 입력하고 접속하는 닉네임)
	public String id = new String(); //서버에서 제공하는 id - 인덱싱 용도
	public PrintWriter writer; //PrintWriter는 클라이언트에게 메세지를 전달 
	
	ClientData(String _id, String nick, PrintWriter w){
		nickname = nick;
		id = _id;
		writer = w;
	}
}

//채팅 서버 클래스 
public class server {
	private int ClientsIndex = 0; // 접속할 클라이언트의 고유 id를 주기 위한 인덱싱
	private ArrayList clients; // 클라이언트 리스트

	// 클라이언트 핸들러 클래스
	// - 클라이언트에게 메시지 전달 역할을 수행
	// - 이때 XML형식의 데이터를 파싱하여 필요시 메시지 생성 후 전달
	public class ClientHandler implements Runnable
	{
		private BufferedReader reader; // 클라이언트에게 메시지를 받는 Reader
		private Socket sock; // 클라이언트의 Socket
		private String clientID; // 클라이언트의 id

		// 기본핸들러에는 소켓과 id를 저장
		public ClientHandler(Socket clientSocket, String _id)
		{
			clientID = _id;
			try
			{
				sock = clientSocket;
				InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				reader = new BufferedReader(isReader);
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}

		// Thread를 실행할시 수행할 부분
		public void run()
		{
			String message;
			try
			{
				// 클라이언트가 메시지를 전달할때까지 블락상태에 있다가
				// 클라이언트가 메시지를 전달하면 수행
				while ( (message = reader.readLine()) != null )
				{
					// 클라이언트에서 전달해온 메시지 출력
					// - 서버 콘솔에서 확인하기 위함
					System.out.println("read " + message);

					// 전달해온 클라이언트의 닉네임을 파싱
					int index1 = message.indexOf("<NICKNAME>");
					int index2 = message.indexOf("</NICKNAME>");
					String curNick = message.substring(index1+10, index2);
					
					// 닉네임 뒤쪽의 데이터를 파싱
					String back_message = message.substring(index2+11, message.length());
				
					String ConnectMessage = new String();
					// <NEW_LOGIN>이라는 문자열이 존재한다면 새로운 접속
					if( (back_message.indexOf("<NEW_LOGIN>")) != -1 )
					{
						// 새로운 접속
						ConnectMessage = "<NICKNAME>" + curNick + "</NICKNAME><CONNECT_LIST>" + curNick;

						// 현재 클라이언트리스트에 있는 모든 닉네임을 클라이언트들에게 알려줌
						// - 클라이언트의 접속자 리스트 갱신을 위함임
						Iterator it = clients.iterator();
						while( it.hasNext() )
						{
							ClientData client = (ClientData) it.next();
							ConnectMessage = ConnectMessage + "|" + client.nickname;
						}
						ConnectMessage = ConnectMessage + "</CONNECT_LIST>";
						// 접속자 메시지 전달
						tellEveryone(ConnectMessage);
					}
					// <MYID>라는 문자열이 존재한다면
					// - 서버측에서 알려준 ID에 대한 닉네임을 의미함
					else if( (back_message.indexOf("<MYID>")) != -1 )
					{
						// 아이디 저장
						index1 = back_message.indexOf("<MYID>");
						index2 = back_message.indexOf("</MYID>");
						if( index1 == -1 || index2 == -2 )
						{
							continue;
						}
						String id = back_message.substring(index1+6,index2);

						// 클라이언트 리스트에서 id에 해당하는 클라이언트를 찾아
						// 현재 메시지의 닉네임을 클라이언트의 닉네임으로 저장
						Iterator it = clients.iterator();
						while( it.hasNext() )
						{
							ClientData client = (ClientData) it.next();
							if( client.id.compareTo(id) != 0 )
							{
								continue;
							}
							client.nickname = curNick;
							break;
						}
						System.out.println("id = " + id + " / Nick = " + curNick);

						continue;
					}
					// 메시지 전달
					tellEveryone(message);
				}
			}
			// SocketException은 클라이언트와 서버간의 연결이 끊겼을때 발생
			// - 접속종료, - 강제종료, - 인터넷 연결종료 등
			catch(SocketException e)
			{
				// 해당 클라이언트 제거
				// - 클라이언트 ID를 이용하여 해당 클라이언트의 닉네임 산출
				// - 그리고 해당 클라이언트를 리스트에서 삭제
				String exitClientNick = new String();
				for(int i=0; i<clients.size(); ++i)
				{
					ClientData client = (ClientData) clients.get(i);
					if( client.id.compareTo(clientID) != 0 )
					{
						continue;
					}
					exitClientNick = client.nickname;
					clients.remove(i);
					break;
				}

				// 클라이언트들에게 로그아웃한 클라이언트를 제외한 나머지 클라이언트의 닉네임 리스트를 전달
				String ConnectMessage = new String();
				ConnectMessage = "<NICKNAME>SERVER</NICKNAME><CONNECT_LIST>";

				Iterator it = clients.iterator();

				if( it.hasNext() )
				{
					ClientData client = (ClientData) it.next();
					ConnectMessage = ConnectMessage + client.nickname;
				}
				while( it.hasNext() )
				{
					ClientData client = (ClientData) it.next();
					ConnectMessage = ConnectMessage + "|" + client.nickname;
				}
				ConnectMessage = ConnectMessage + "</CONNECT_LIST>";
				tellEveryone(ConnectMessage);

				// 그리고 logout한 클라이언트의 닉네임을 알려줌
				message = "<NICKNAME>SERVER</NICKNAME><LOGOUT>"+ exitClientNick +"</LOGOUT>";
				tellEveryone(message);
			}
			// 그 외 예외 발생시 예외 사항 출력
			catch( Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void main(String Args[])
	{
		new server().excuteServer();
	}

	public void excuteServer()
	{
		// 클라이언트 리스트를 초기화
		clients = new ArrayList();
		try
		{
			// 서버 오픈 ( 포트번호 5000 )
			ServerSocket serverSock = new ServerSocket(5000);
			while( true )
			{
				// 새로운 클라이언트가 등장하면 clinetSocket에 접속한 클라이언트의 소켓을 저장
				Socket clientSocket = serverSock.accept();
				PrintWriter writer = new PrintWriter(clientSocket.getOutputStream()); // PrintWriter산출
				clients.add(new ClientData(new String(""+ClientsIndex), "", writer) ); // 클라이언트리스트 추가
				// 클라이언트에게 ID를 알려줌
				// - 이는 클라이언트에게 자신의 아이디를 알려주고
				// - 클라이언트의 닉네임을 알기위함임
				writer.println("<YOURID>"+ClientsIndex+"</YOURID>");
				writer.flush();

				// 해당 클라이언트의 소켓을 이용하여 클라이언트 핸들러 스레드 시작
				Thread thread = new Thread(new ClientHandler(clientSocket, new String(""+ClientsIndex)));
				thread.start();

				System.out.println("got a connection");

				// 클라이언트 인덱스 증가
				ClientsIndex++;
			}
		}
		// 예외 발생시 예외사항 출력
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	// 클라이언트들에게 메시지를 전달하는 메소드
	public void tellEveryone( String message )
	{
		// 모든 클라이언트들에게 반복
		Iterator it = clients.iterator();
		while( it.hasNext() )
		{
			try
			{
				// 클라이언트리스트에서 하나의 클라이언트를 가져옴
				ClientData client = (ClientData) it.next();
				client.writer.println(message); // 해당 클라이언트의 PrintWriter에 해당 메시지를 보냄
				client.writer.flush(); // PrintWriter를 Flush함. 비워주는 것
			}
			// 예외 발생시 예외사항 출력
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
	}
	
}

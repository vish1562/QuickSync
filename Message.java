import java.io.*;

class Message implements Serializable {
	Object obj;
	int len;

	Message(Object obj) {
		this.obj = obj;
	}
}
Android App to send photos by sms.

Fundemental Flaw: even a max dimension of > 500px jpg = ~800 text messages, at 160 char limit (not counting indexing characters).
On testing, sprint has disabled my ability to send out text messages all together at around text #60.

Potential resolution: send 20 messages every 5 min. (Android service);
		      send messages in bulk, the 900 char limit. (risky- do not know where messages will be cut off)
		      other encoding schemes
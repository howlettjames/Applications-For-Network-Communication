#include <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <time.h>
#include <sys/socket.h>
#include <netdb.h>
#include <arpa/inet.h>

#define BUF_SIZE 500
#define MAX_RAND 3

int main(int argc, char *argv[])
{
	//SOCKET LOGIC
	struct addrinfo hints;
	struct addrinfo *result, *rp;
	const char *port = "9999";
	int sock_descriptor, error_code, option = 1;
	struct sockaddr_storage peer_addr;
	socklen_t peer_addr_len = sizeof(struct sockaddr_storage);
	ssize_t nread;
	char buf[BUF_SIZE], host[NI_MAXHOST], service[NI_MAXSERV];
	//PROGRAM LOGIC
	short difficulty;
	char *easy_strings[] = {"home", "run", "stuff"};
	char *medium_strings[] = {"running", "datagram", "stratocaster"};
	char *hard_strings[] = {"flying away from you", "making up some noise", "rest in peace"};

	srand((unsigned) time(NULL));

	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family = AF_INET6;					//Allows IPv4 and IPv6
	hints.ai_socktype = SOCK_DGRAM;				//Datagram Socket
	hints.ai_flags = AI_PASSIVE;				//For wildcard IP address also because this is the server
	hints.ai_protocol = 0; 						//Any protocol
	hints.ai_canonname = NULL;
	hints.ai_addr = NULL;
	hints.ai_next = NULL;

	error_code = getaddrinfo(NULL, port, &hints, &result);
	if(error_code != 0)
	{
		fprintf(stderr, "getaddrinfo: %s\n", gai_strerror(error_code));
		exit(EXIT_FAILURE);	
	}

	/*getaddrinfo() returns a list of address structures.
	  Try each address until we successfully bind(2).
	  If socket(2) (or bind(2)) fails, we (close the socket)
	  and try the next address.*/
	
	for(rp = result; rp != NULL; rp = rp->ai_next)
	{
		sock_descriptor = socket(rp->ai_family, rp->ai_socktype, rp->ai_protocol);
		if(sock_descriptor == -1)
			continue;

		option = 0;
		if(setsockopt(sock_descriptor, IPPROTO_IPV6, IPV6_V6ONLY, &option, sizeof(option)) == -1)
		{
			perror("setsockopt");
			exit(1);
		}

		option = 1;
		if(setsockopt(sock_descriptor, SOL_SOCKET, SO_REUSEADDR, &option, sizeof(int)) == -1)
		{
			perror("setsockopt");
			exit(1);
		}

		//Succed
		if(bind(sock_descriptor, rp->ai_addr, rp->ai_addrlen) == 0)
			break;

		close(sock_descriptor);
	}

	//No address succeded
	if(rp == NULL)
	{
		fprintf(stderr, "Could not bind\n");
		exit(EXIT_FAILURE);
	}
	//No longer needed
	freeaddrinfo(result);

	printf("Server running...\n");

	//READ DATAGRAMS AND ECHO THEM BACK TOGETHER
	for(;;)
	{
		nread = recvfrom(sock_descriptor, &difficulty, sizeof(short), 0, (struct sockaddr *) &peer_addr, &peer_addr_len);
		if(nread == -1)
		{
			fprintf(stderr, "Failed recvfrom\n");
			continue; //Ignore failed request
		}

		error_code = getnameinfo((struct sockaddr *) &peer_addr, peer_addr_len, host, NI_MAXHOST, service, NI_MAXSERV, NI_NUMERICSERV);
		if(error_code == 0)
		{
			//char *end;
			//difficulty = strtol(buf, &end, 0);
			difficulty = ntohs(difficulty);
			printf("Message received from %s:%s -> Bytes received: %ld -> Selected difficulty: %hd\n", host, service, (long) nread, difficulty);

			if(difficulty == 1)
			{
				strcpy(buf, easy_strings[rand() % MAX_RAND]);
				printf("Word: %s\n", buf);
				if(sendto(sock_descriptor, buf, strlen(buf) + 1, 0, (struct sockaddr *) &peer_addr, peer_addr_len) == -1)
					fprintf(stderr, "Error sending response\n");		
			}
			else if(difficulty == 2)
			{
				strcpy(buf, medium_strings[rand() % MAX_RAND]);
				printf("Word: %s\n", buf);
				if(sendto(sock_descriptor, buf, strlen(buf) + 1, 0, (struct sockaddr *) &peer_addr, peer_addr_len) == -1)
					fprintf(stderr, "Error sending response\n");		
			}
			else if(difficulty == 3)
			{
				strcpy(buf, hard_strings[rand() % MAX_RAND]);
				printf("Word: %s\n", buf);
				if(sendto(sock_descriptor, buf, strlen(buf) + 1, 0, (struct sockaddr *) &peer_addr, peer_addr_len) == -1)
					fprintf(stderr, "Error sending response\n");		
			}
		}
		else
			fprintf(stderr, "getnameinfo: %s\n", gai_strerror(error_code));
	}
}	
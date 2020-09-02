#include <stdio.h>			//printf(), perror()
#include <stdlib.h>			//exit()
#include <string.h>			
#include <unistd.h>			//read(), write()
#include <netdb.h>			//getadrrinfo()
#include <sys/types.h>
#include <sys/socket.h>

#define BUF_SIZE 500

int main(int argc, char *argv[])
{
	struct addrinfo hints;
	struct addrinfo *result, *rp;
	int socket_descriptor, error_code, j;
	size_t len;
	ssize_t nread;
	char buf[BUF_SIZE];
	const char *ip = "localhost";
	const char *port = "9999";

	if(argc < 2)
	{
		fprintf(stderr, "Usage: <%s> <message>\n", argv[0]);
		exit(EXIT_FAILURE);
	}

	//OBTAIN ADDRESS(ES) MATCHING HOST AND PORT
	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family = AF_UNSPEC; 				//Allow IPv4 and IPv6
	hints.ai_socktype = SOCK_DGRAM;				//Using datagram socket
	hints.ai_flags = 0;
	hints.ai_protocol = 0;						//Any protocol

	error_code = getaddrinfo(ip, port, &hints, &result);
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
		socket_descriptor = socket(rp->ai_family, rp->ai_socktype, rp->ai_protocol);
		if(socket_descriptor == -1)
			continue;

		if(connect(socket_descriptor, rp->ai_addr, rp->ai_addrlen) != -1) //Success
			break;

		close(socket_descriptor);
	}

	if(rp == NULL)	//No address succeded
	{
		fprintf(stderr, "Could not connect\n");
		exit(EXIT_FAILURE);
	}

	freeaddrinfo(result);		//No longer needed

	//Send remaining command-line arguments as separate 
	//datagrams, and read responses from server
	for(j = 1; j < argc; j++)
	{
		len = strlen(argv[j]) + 1; 		//+1 for terminating null byte

		if(len + 1 > BUF_SIZE)
		{
			fprintf(stderr, "Ignoring long message in argument %d\n", j);
			continue;
		}

		if(write(socket_descriptor, argv[j], len) != len)
		{
			fprintf(stderr, "partial/failed write\n");
			exit(EXIT_FAILURE);
		}

		nread = read(socket_descriptor, buf, BUF_SIZE);
		if(nread == -1)
		{
			perror("read");
			exit(EXIT_FAILURE);
		}

		printf("Received echo -> %ld bytes: %s\n", (long) nread, buf);
	}

	exit(EXIT_SUCCESS);
}	
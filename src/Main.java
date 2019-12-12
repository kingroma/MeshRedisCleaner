import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;


// $sed -i -e 's/\r$//' start.sh
// java -jar MeshRedisCleaner.jar SBVC0-MsgRelay1 127.0.0.1
public class Main {
	public static void main(String[] args) {
		try {
			if ( args != null && args.length == 2 
					&& args[0] != null && !args[0].isEmpty()
					&& args[1] != null && !args[1].isEmpty()
					) {
				
				String serverName = args[0] ; // "SBVCP-MsgRelay1";
				String redisIp = args[1] ; // "127.0.0.1";
				int redisPort = 6379 ;
				
				System.out.println("Cleaner Start [ " + redisIp + " / " + serverName + " ] ");
				
				JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		        JedisPool pool = new JedisPool(jedisPoolConfig, redisIp , redisPort, 1000, null);
		        Jedis jedis = pool.getResource();
		        
		        String hashKey = "conn:xmpp:" + serverName ;
		        System.out.println("delete > " + hashKey);
		        jedis.del(hashKey);
		        
		        String serverListKey = "conn:serverlist";
		        System.out.println("sadd > " + serverName);
		        jedis.sadd(serverListKey, serverName);
		        
		        ScanParams scanParams = new ScanParams().count(10).match("conn:xmpp:devices:*");
		        String cur = redis.clients.jedis.ScanParams.SCAN_POINTER_START;
		        int count = 0 ;
		        do {
		            ScanResult<String> scanResult = jedis.scan(cur, scanParams);
		            List<String> list = scanResult.getResult();
		            
		            for ( String key : list ) { 
		            	String value = jedis.get(key);
		            	if ( value != null && !value.isEmpty() && value.equalsIgnoreCase(serverName) && !"conn:xmpp:devices:".equals(key)) {
		            		jedis.del(key);
		            		count ++ ;
		            	}
		            }
		            
		            cur = scanResult.getStringCursor();
		        } while (!cur.equals(cur));
		        System.out.println("delete conn:xmpp:devices:uuid count > [ " + count + " ]");
		        System.out.println("finish cleaner");
		        
		        jedis.close();
		        pool.close();
			}else {
				System.out.println("PARAM ERROR Please Retry [ 1) ServerName , 2) IP ] ");
			}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

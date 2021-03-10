package com.jzp.task.revolver;

import sun.net.util.IPAddressUtil;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuzk on 2017/8/2.
 */
public class IPUtils {

    /**
     * 10.x.x.x/8
     */
    private static final byte SECTION_10 = 0x0A;
    /**
     * 172.16.x.x/12
     */
    private static final byte SECTION_172 = (byte) 0xAC;
    private static final byte SECTION_16 = (byte) 0x10;
    private static final byte SECTION_31 = (byte) 0x1F;
    /**
     * 192.168.x.x/16
     */
    private static final byte SECTION_192 = (byte) 0xC0;
    private static final byte SECTION_168 = (byte) 0xA8;

    private static final List<String> localIpList = Arrays.asList("127.0.0.1", "localhost");

//    /**
//     * 获取用户的请求IP
//     *
//     * @param request
//     * @return
//     * @throws Exception
//     */
//
//    public static String[] getIPAddressArray(HttpServletRequest request) throws Exception {
//        String ip = request.getHeader("Cdn-Src-Ip");
//        //x-forwarded-for是用来跟踪原有的客户端IP地址和原来客户端请求的服务器地址
//        //所以为了防止客户端进行了代理跳转，先取x-forwarded-for
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("x-forwarded-for");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("Proxy-Client-IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("x-real-ip");
//        }
//        //代理取不到，则取getRemoteAddr
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getRemoteAddr();
//        }
//        /**
//         * 如果客户进行了多级代理，取到的应该是如：
//         * 		X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
//         * 这样的信息，此此时应该取第一个非unknown的有效IP字符串
//         */
//        if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
//            if (ip.contains(",")) {
//                String[] ipArr = ip.split(",");
//                return ipArr;
//            } else {
//                return new String[]{ip};
//            }
//        }
//        return null;
//    }
//
//    public static String getIPAddress(HttpServletRequest request) throws Exception {
//        String[] ipArr = getIPAddressArray(request);
//        if (ipArr == null || ipArr.length == 0) {
//            return "";
//        }
//        for (String _ip : ipArr) {
//            if (_ip != null && _ip.trim().length() > 0 && !"unknown".equalsIgnoreCase(_ip)) {
//                return _ip.trim();
//            }
//        }
//        return null;
//    }

    /**
     * 获取本机ip
     *
     * @return
     * @throws Exception
     */
    public static String getHostAddress() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();
        return ip;
    }

    /**
     * 获取本机计算机名称
     *
     * @return
     * @throws Exception
     */
    public static String getHostName() throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        String hostName = addr.getHostName().toString();
        return hostName;
    }

    /**
     * ip格式字符串转长整型
     *
     * @param strIp
     * @return
     */
    public static long ipToLong(String strIp) {
        long[] ip = new long[4];
        // 先找到IP地址字符串中.的位置
        int position1 = strIp.indexOf(".");
        int position2 = strIp.indexOf(".", position1 + 1);
        int position3 = strIp.indexOf(".", position2 + 1);
        // 将每个.之间的字符串转换成整型
        ip[0] = Long.parseLong(strIp.substring(0, position1));
        ip[1] = Long.parseLong(strIp.substring(position1 + 1, position2));
        ip[2] = Long.parseLong(strIp.substring(position2 + 1, position3));
        ip[3] = Long.parseLong(strIp.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
    }

    /**
     * 长整型转ip格式字符串
     *
     * @param longIp
     * @return
     */
    public static String longToIP(long longIp) {
        StringBuilder sb = new StringBuilder();
        // 直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

    /**
     * 本地IP
     *
     * @param ip
     * @return
     */
    public static boolean isLocal(String ip) {
        return localIpList.contains(ip);
    }

    /**
     * 内网IP段
     *
     * @param ip
     * @return
     */
    public static boolean isInternal(String ip) {
        byte[] addr = IPAddressUtil.textToNumericFormatV4(ip);
        return internalIp(addr);
    }


    public static boolean internalIp(byte[] addr) {
        final byte b0 = addr[0];
        final byte b1 = addr[1];

        switch (b0) {
            case SECTION_10:
                return true;
            case SECTION_172:
                if (b1 >= SECTION_16 && b1 <= SECTION_31) {
                    return true;
                }
            case SECTION_192:
                switch (b1) {
                    case SECTION_168:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;

        }
    }
}

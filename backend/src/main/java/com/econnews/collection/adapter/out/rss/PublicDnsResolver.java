package com.econnews.collection.adapter.out.rss;

import java.net.*;
import org.apache.hc.client5.http.DnsResolver;

/** Validate the actual DNS results used by the HTTP connection, not a separate preflight lookup. */
public class PublicDnsResolver implements DnsResolver {

    @Override
    public InetAddress[] resolve(String host) throws UnknownHostException {
        InetAddress[] addresses = InetAddress.getAllByName(host);
        if (addresses.length == 0) throw new UnknownHostException("RSS host has no addresses");
        for (InetAddress address : addresses)
            if (!isPublic(address)) throw new UnknownHostException("RSS destination is not public");
        return addresses;
    }

    @Override
    public String resolveCanonicalHostname(String host) {
        return host;
    }

    public static boolean isPublic(InetAddress address) {
        if (
            address.isAnyLocalAddress() ||
            address.isLoopbackAddress() ||
            address.isLinkLocalAddress() ||
            address.isSiteLocalAddress() ||
            address.isMulticastAddress()
        ) return false;
        byte[] raw = address.getAddress();
        if (raw.length == 4) {
            int a = raw[0] & 255,
                b = raw[1] & 255,
                c = raw[2] & 255;
            return !(
                a == 0 ||
                a == 10 ||
                a == 127 ||
                a >= 224 ||
                (a == 100 && b >= 64 && b <= 127) ||
                (a == 169 && b == 254) ||
                (a == 172 && b >= 16 && b <= 31) ||
                (a == 192 &&
                    (b == 168 || (b == 0 && (c == 0 || c == 2)) || (b == 88 && c == 99))) ||
                (a == 198 && (b == 18 || b == 19 || (b == 51 && c == 100))) ||
                (a == 203 && b == 0 && c == 113)
            );
        }
        // Allow only global unicast IPv6; reject transition/documentation/special ranges conservatively.
        int a = raw[0] & 255,
            b = raw[1] & 255,
            c = raw[2] & 255,
            d = raw[3] & 255;
        return (
            (a & 0xe0) == 0x20 &&
            !(a == 0x20 && b == 0x01 && (c <= 1 || (c == 0x0d && d == 0xb8))) &&
            !(a == 0x20 && b == 0x02) &&
            !(a == 0x3f && b >= 0xf0)
        );
    }
}

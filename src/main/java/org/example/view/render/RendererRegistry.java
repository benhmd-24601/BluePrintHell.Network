package org.example.view.render;

import org.example.model.Packet.Packet;
import org.example.model.Systems.NetworkSystem;
import org.example.view.render.packets.PacketRenderer;
import org.example.view.render.systems.SystemRenderer;

import java.util.HashMap;
import java.util.Map;

public class RendererRegistry {
    private final Map<Class<?>, PacketRenderer<?>> packetMap = new HashMap<>();
    private final Map<Class<?>, SystemRenderer<?>> systemMap = new HashMap<>();

    public <T extends Packet> void registerPacketRenderer(Class<T> type, PacketRenderer<? super T> renderer) {
        packetMap.put(type, renderer);
    }
    public <S extends NetworkSystem> void registerSystemRenderer(Class<S> type, SystemRenderer<? super S> renderer) {
        systemMap.put(type, renderer);
    }

    @SuppressWarnings("unchecked")
    public <T extends Packet> PacketRenderer<T> findPacketRenderer(T obj) {
        PacketRenderer<?> r = search(packetMap, obj.getClass());
        if (r == null) throw new IllegalStateException("No PacketRenderer for " + obj.getClass().getName());
        return (PacketRenderer<T>) r;
    }

    @SuppressWarnings("unchecked")
    public <S extends NetworkSystem> SystemRenderer<S> findSystemRenderer(S obj) {
        SystemRenderer<?> r = search(systemMap, obj.getClass());
        if (r == null) throw new IllegalStateException("No SystemRenderer for " + obj.getClass().getName());
        return (SystemRenderer<S>) r;
    }

    private <R> R search(Map<Class<?>, R> map, Class<?> cls) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            R r = map.get(c);
            if (r != null) return r;
        }
        // این fallback اجازه می‌دهد روی کلاس‌های والد (Packet.class/NetworkSystem.class) برگردیم اگر ثبت شده باشد
        return null;
    }
}

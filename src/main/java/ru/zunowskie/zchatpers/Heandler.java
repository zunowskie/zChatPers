package ru.zunowskie.zchatpers;

import com.ubivashka.vk.bukkit.BukkitVkApiPlugin;
import com.ubivashka.vk.bukkit.events.VKMessageEvent;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class Heandler implements Listener {

    private static final VkApiClient CLIENT = BukkitVkApiPlugin.getPlugin(BukkitVkApiPlugin.class).getVkApiProvider()
            .getVkApiClient();
    private static final GroupActor ACTOR = BukkitVkApiPlugin.getPlugin(BukkitVkApiPlugin.class).getVkApiProvider()
            .getActor();
    private final static Random RANDOM = new Random();


    @EventHandler
    public void onGetPeer(VKMessageEvent e) {
        String message = e.getMessage().getText();
        if (message.startsWith("?getid")) {
            try {
                CLIENT.messages().send(ACTOR)
                        .randomId(RANDOM.nextInt())
                        .peerId(e.getPeer())
                        .message("👌 Айди этого чата: " + e.getPeer())
                        .execute();
            } catch (ApiException | ClientException ex) {
                throw new RuntimeException(ex);
            }
            return;
        }
    }


    @EventHandler
    public void onSend(VKMessageEvent e) {
        String adminId = String.valueOf(e.getUserId());
        String message = e.getMessage().getText();
        if (message != null && message.startsWith("?отправить")) {
            int peerId = e.getPeer();
            int allowedPeerId = ZChatPers.getInstance().getConfig().getInt("vk.peerid");

            if (peerId != allowedPeerId) {
                try {
                    CLIENT.messages().send(ACTOR)
                            .randomId(RANDOM.nextInt())
                            .peerId(e.getPeer())
                            .message("😒В данной беседе эта команда недоступна. Поддержка @capojkiii")
                            .execute();
                } catch (ApiException | ClientException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            String textToSend = message.substring(10); 
            if (textToSend.isEmpty()) {
                try {
                    CLIENT.messages().send(ACTOR)
                            .randomId(RANDOM.nextInt())
                            .peerId(e.getPeer())
                            .message("Используйте ?отправить сообщение")
                            .execute();
                } catch (ApiException | ClientException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            List<Player> admins = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.hasPermission("zStaffWork.chat"))
                    .collect(Collectors.toList());

            String adminNick = ZChatPers.getInstance().getConfig().getString("admins." + adminId);
            if (adminNick == null) {
                try {
                    CLIENT.messages().send(ACTOR)
                            .randomId(RANDOM.nextInt())
                            .peerId(e.getPeer())
                            .message("😊Вашего ника нету в конфигурации плагина. Обратитесь в поддержку @capojkiii")
                            .execute();
                } catch (ApiException | ClientException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            // Отправляем сообщение каждому администратору
            for (Player admin : admins) {
                admin.sendMessage(ZChatPers.getInstance().getConfig().getString("vk.message")
                        .replace("%msg%", textToSend)
                        .replace("%target%", adminNick)
                        .replace("&", "§"));
            }

            try {
                CLIENT.messages().send(ACTOR)
                        .randomId(RANDOM.nextInt())
                        .peerId(e.getPeer())
                        .message("👌вы отправили сообщение")
                        .execute();
            } catch (ApiException | ClientException ex) {
                throw new RuntimeException(ex);
            }
        }
    }



    @EventHandler(priority = EventPriority.HIGH)
    public void onChat(AsyncPlayerChatEvent e) {
        String message = e.getMessage();
        if (!message.startsWith("#")) {
            return;
        }
        if (ZChatPers.getInstance().getConfig().getString("vk.peerid") == null && ZChatPers.getInstance().getConfig().getString("vk.peerid").isEmpty()) {
            e.getPlayer().sendMessage("Поле vk.peerid было не найдено или пустое. Поддержка @capojkiii ");
            return;

        }

        Player player = e.getPlayer();
        if (player.hasPermission("zChatPers.chat")) {
            e.setCancelled(true);
            String msg = message.substring(1);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("zChatPers.chat")) {
                    p.sendMessage(ZChatPers.getInstance().getConfig().getString("message.style")
                            .replace("&", "§")
                            .replace("%msg%", msg)
                            .replace("%player%", player.getDisplayName()));
                }
            }
            if (ZChatPers.getInstance().getConfig().getBoolean("message.vk")) {
                String msgvk = ZChatPers.getInstance().getConfig().getString("message.vkmsg")
                        .replace("%msg%", msg)
                        .replace("%player%", player.getDisplayName());
                try {
                    CLIENT.messages().send(ACTOR)
                            .randomId(RANDOM.nextInt())
                            .peerId(ZChatPers.getInstance().getConfig().getInt("vk.peerid"))
                            .message(msgvk)
                            .execute();
                } catch (ClientException | ApiException a) {
                    throw new RuntimeException(a);
                }
            }
        }
    }
}

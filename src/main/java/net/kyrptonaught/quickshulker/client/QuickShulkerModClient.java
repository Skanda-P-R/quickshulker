package net.kyrptonaught.quickshulker.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.kyrptconfig.keybinding.CustomKeyBinding;
import net.kyrptonaught.kyrptconfig.keybinding.DisplayOnlyKeyBind;
import net.kyrptonaught.quickshulker.QuickShulkerMod;
import net.kyrptonaught.quickshulker.api.RegisterQuickShulkerClient;
import net.kyrptonaught.quickshulker.network.OpenInventoryPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public class QuickShulkerModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_WORLD_TICK.register(clientWorld -> {
            if (MinecraftClient.getInstance().currentScreen == null && QuickShulkerMod.getConfig().keybind) {
                PlayerEntity player = MinecraftClient.getInstance().player;
                if (getKeybinding().isKeybindPressed() && player != null) {
                    if (player.getMainHandStack().isEmpty() && !player.getOffHandStack().isEmpty())
                        ClientUtil.CheckAndSend(player.getOffHandStack(), 45);
                    else
                        ClientUtil.CheckAndSend(player.getMainHandStack(), 36 + player.getInventory().getSelectedSlot());
                }
            }
        });

        PayloadTypeRegistry.playS2C().register(OpenInventoryPacket.ID, OpenInventoryPacket.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(OpenInventoryPacket.ID, (payload, context) -> {
            context.client().setScreen(
                    new InventoryScreen(context.player())
            );
        });
        FabricLoader.getInstance().getEntrypoints(QuickShulkerMod.MOD_ID + "_client", RegisterQuickShulkerClient.class).forEach(RegisterQuickShulkerClient::registerClient);

        KeyBindingHelper.registerKeyBinding(new DisplayOnlyKeyBind(
                "key.quickshulker.config.keybinding",
                "key.categories.quickshulker",
                getKeybinding(),
                setKey -> QuickShulkerMod.config.save()
        ));
    }

    public static CustomKeyBinding getKeybinding() {
        return QuickShulkerMod.getConfig().keybinding;
    }
}
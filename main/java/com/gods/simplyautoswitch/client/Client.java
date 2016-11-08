package com.gods.simplyautoswitch.client;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Client {
	
	private static final Minecraft mc = Minecraft.getMinecraft();

	private static ItemStack prevHeldItem = null;

	public static boolean enabled = true;
	
	private static void ChangeHeldItem(ItemStack itemstack) {
		int iSlot = mc.thePlayer.inventory.currentItem;
		
		prevHeldItem = mc.thePlayer.inventory.mainInventory[iSlot];
		mc.thePlayer.inventory.mainInventory[iSlot] = itemstack;
		
		if (prevHeldItem != null) mc.thePlayer.getAttributeMap().removeAttributeModifiers(prevHeldItem.getAttributeModifiers(null));
		if (itemstack != null) mc.thePlayer.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers(null));
	}

	private static void RestoreHeldItem() {
		int iSlot = mc.thePlayer.inventory.currentItem;
		
		ItemStack itemstack = mc.thePlayer.inventory.mainInventory[iSlot];
		mc.thePlayer.inventory.mainInventory[iSlot] = prevHeldItem;
		
		if (itemstack != null) mc.thePlayer.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers(null));
		if (prevHeldItem != null) mc.thePlayer.getAttributeMap().applyAttributeModifiers(prevHeldItem.getAttributeModifiers(null));
	}
	
	public static float getBlockStrength(ItemStack itemstack, World world, BlockPos oPos) {
		IBlockState state = world.getBlockState(oPos);
		ChangeHeldItem(itemstack);
		float strength = state.getPlayerRelativeBlockHardness(mc.thePlayer, world, oPos);
		RestoreHeldItem();
		return strength;
	}

	public static float getDigSpeed(ItemStack itemstack, IBlockState state) {
		return (itemstack == null ? 1.0F : itemstack.getItem().getStrVsBlock(itemstack, state));
	}

	public static boolean isItemStackDamageable(ItemStack itemstack) {
		return (itemstack != null && itemstack.getItem().isDamageable());
	}
	
	public static double getVanillaStackDamage(ItemStack itemStack, EntityLivingBase entity) {
		System.out.println(mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue());
		ChangeHeldItem(itemStack);
		
		double attackDamage = mc.thePlayer.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
		double enchantModifier = EnchantmentHelper.getModifierForCreature(itemStack, entity.getCreatureAttribute());

		if (attackDamage > 0.0D || enchantModifier > 0.0D) {
			boolean isCritical = mc.thePlayer.fallDistance > 0.0F &&
								!mc.thePlayer.onGround &&
								!mc.thePlayer.isOnLadder() &&
								!mc.thePlayer.isInWater() &&
								!mc.thePlayer.isPotionActive(MobEffects.BLINDNESS) &&
								 mc.thePlayer.getRidingEntity() == null;

			if (isCritical && attackDamage > 0) attackDamage *= 1.5D;

			attackDamage += enchantModifier;
		}
		
		RestoreHeldItem();
		
		return attackDamage;
	}
	
	public static boolean determineTool(ItemStack CurrentTool, ItemStack CompareTool, World world, BlockPos oPos) {
		
		IBlockState state = world.getBlockState(oPos);
		Block block = state.getBlock();

		if (block == null || block == Blocks.AIR || block == Blocks.BEDROCK) return false;
		
		float currentBlockStrength = getBlockStrength(CurrentTool, world, oPos);
		float compareBlockStrength = getBlockStrength(CompareTool, world, oPos);
		
		if (currentBlockStrength <= 0F && compareBlockStrength <= 0F) return false;

		float currentDigSpeed = getDigSpeed(CurrentTool, state);
		float compareDigSpeed = getDigSpeed(CompareTool, state);
		
		boolean currentToolDamageable = isItemStackDamageable(CurrentTool);
		boolean compareToolDamageable = isItemStackDamageable(CompareTool);
		
		if (currentToolDamageable && !compareToolDamageable && compareDigSpeed >= currentDigSpeed) return true;
		else if (compareDigSpeed >= currentDigSpeed) return true;
		else if (compareDigSpeed < currentDigSpeed) return false;
		return false;
	}
	
	public static boolean determineWeapon(ItemStack CurrentWeapon, ItemStack CompareWeapon, EntityLivingBase entity) {


		double currentWeaponDamage = Client.getVanillaStackDamage(CurrentWeapon, entity);
		double compareWeaponDamage = Client.getVanillaStackDamage(CompareWeapon, entity);
		//System.out.println(currentWeaponDamage + " " + compareWeaponDamage);

	
		if (compareWeaponDamage > currentWeaponDamage) return true;
		else if (compareWeaponDamage < currentWeaponDamage) return false;


		return false;
	}
	
}
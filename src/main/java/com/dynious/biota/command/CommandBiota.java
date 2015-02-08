package com.dynious.biota.command;

import com.dynious.biota.biosystem.BioSystem;
import com.dynious.biota.biosystem.BioSystemHandler;
import com.dynious.biota.biosystem.BioSystemInitThread;
import com.dynious.biota.lib.Commands;
import com.dynious.biota.network.NetworkHandler;
import com.dynious.biota.network.message.MessageBioSystemUpdate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;

import java.util.Iterator;
import java.util.List;

public class CommandBiota extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return Commands.BIOTA;
    }

    @Override
    public String getCommandUsage(ICommandSender icommandsender)
    {
        return "/" + getCommandName() + " " + Commands.HELP;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return true;
    }

    @Override
    public void processCommand(ICommandSender icommandsender, String[] args)
    {
        if (args.length > 0)
        {
            String commandName = args[0];

            if (commandName.equalsIgnoreCase(Commands.HELP))
            {
                //Halp
            }
            else if (commandName.equalsIgnoreCase(Commands.GET_NUTRIENTS))
            {
                World world = icommandsender.getEntityWorld();
                Chunk chunk = world.getChunkFromBlockCoords(icommandsender.getPlayerCoordinates().posX, icommandsender.getPlayerCoordinates().posZ);
                BioSystem bioSystem = BioSystemHandler.getBioSystem(chunk);
                if (bioSystem != null)
                {
                    icommandsender.addChatMessage(new ChatComponentText(String.format("Phosphorus: %f, Potassium: %f, Nitrogen %f", bioSystem.getPhosphorus(), bioSystem.getPotassium(), bioSystem.getNitrogen())));
                }
            }
            else if (commandName.equalsIgnoreCase(Commands.SET_NUTRIENTS))
            {
                if (args.length > 3)
                {
                    World world = icommandsender.getEntityWorld();
                    Chunk chunk = world.getChunkFromBlockCoords(icommandsender.getPlayerCoordinates().posX, icommandsender.getPlayerCoordinates().posZ);
                    BioSystem bioSystem = BioSystemHandler.getBioSystem(chunk);
                    if (bioSystem != null)
                    {
                        bioSystem.setPhosphorus((float) parseDouble(icommandsender, args[1]));
                        bioSystem.setPotassium((float) parseDouble(icommandsender, args[2]));
                        bioSystem.setNitrogen((float) parseDouble(icommandsender, args[3]));
                        System.out.println(bioSystem);

                        NetworkHandler.INSTANCE.sendToPlayersWatchingChunk(new MessageBioSystemUpdate(bioSystem), (WorldServer) world, chunk.xPosition, chunk.zPosition);
                    }
                }
            }
            else if (commandName.equalsIgnoreCase(Commands.GET_LOWEST_IN_WORLD))
            {
                float lowest = Float.MAX_VALUE;
                int x = 0, z = 0;
                Iterator<BioSystem> iterator = BioSystemHandler.iterator();
                while (iterator.hasNext())
                {
                    BioSystem bioSystem = iterator.next();
                    Chunk chunk = bioSystem.chunkReference.get();
                    if (chunk != null && chunk.worldObj.equals(icommandsender.getEntityWorld()))
                    {
                        float value = bioSystem.getLowestNutrientValue();
                        if (value < lowest)
                        {
                            lowest = value;
                            x = chunk.xPosition;
                            z = chunk.zPosition;
                        }
                    }
                }
                icommandsender.addChatMessage(new ChatComponentText(String.format("Lowest nutrient value in loaded world is %f in chunk at %d %d", lowest, x, z)));
            }
            else if (commandName.equalsIgnoreCase(Commands.GET_BIOSYSTEM))
            {
                World world = icommandsender.getEntityWorld();
                Chunk chunk = world.getChunkFromBlockCoords(icommandsender.getPlayerCoordinates().posX, icommandsender.getPlayerCoordinates().posZ);
                BioSystem bioSystem = BioSystemHandler.getBioSystem(chunk);
                if (bioSystem != null)
                {
                    icommandsender.addChatMessage(new ChatComponentText(String.format("Biomass: %f, Nitrogen Fixation: %f, Phosphorus: %f, Potassium: %f, Nitrogen %f, Decomposing Bacteria: %f, Nirtifying Bacteria %f", bioSystem.getBiomass(), bioSystem.getNitrogenFixation(), bioSystem.getPhosphorus(), bioSystem.getPotassium(), bioSystem.getNitrogen(), bioSystem.getDecomposingBacteria(), bioSystem.getNitrifyingBacteria())));
                }
            }
            else if (commandName.equalsIgnoreCase(Commands.RECALCULATE_BIOMASS))
            {
                World world = icommandsender.getEntityWorld();
                Chunk chunk = world.getChunkFromBlockCoords(icommandsender.getPlayerCoordinates().posX, icommandsender.getPlayerCoordinates().posZ);
                BioSystem bioSystem = BioSystemHandler.getBioSystem(chunk);
                if (bioSystem != null)
                {
                    BioSystemInitThread.addBioSystem(bioSystem);
                }
            }
        }
    }

    @Override
    public List addTabCompletionOptions(ICommandSender commandSender, String[] args)
    {
        switch (args.length)
        {
            case 1:
            {
                return getListOfStringsMatchingLastWord(args, Commands.HELP, Commands.SET_NUTRIENTS, Commands.GET_NUTRIENTS, Commands.GET_LOWEST_IN_WORLD, Commands.GET_BIOSYSTEM, Commands.RECALCULATE_BIOMASS);
            }
        }
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(Object obj)
    {
        if (obj instanceof ICommand)
        {
            return this.compareTo((ICommand) obj);
        }
        else
        {
            return 0;
        }
    }
}

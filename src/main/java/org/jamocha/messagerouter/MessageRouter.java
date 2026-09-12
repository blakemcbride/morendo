/**
 * Copyright 2006-2009 Alexander Wilden, Christoph Emonds, Sebastian Reinartz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://ruleml-dev.sourceforge.net/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.jamocha.messagerouter;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.jamocha.rete.Rete;
import org.jamocha.rete.ReturnVector;

/**
 * A MessageRouter is responsible for sending messages to the Rete-engine and
 * receive the answers. Possible MessageListeners will be notified of all events
 * that occured.
 * 
 * @author Alexander Wilden, Christoph Emonds, Sebastian Reinartz
 */
/**
 * Serializes commands from any number of channels onto one command thread and delivers
 * the engine's messages back to the channels that are interested in them. The command
 * thread is the only thread that touches the engine on behalf of the shell and the GUI,
 * which is what makes a single-threaded Rete instance safe to drive from them.
 */
public final class MessageRouter {

	private final Map<String, CommunicationChannel> idToChannel = new HashMap<>();

	private final Map<String, List<MessageEvent>> idToMessages = new LinkedHashMap<>();

	private volatile String currentChannelId = "";

	private final Rete engine;

	private final BlockingQueue<CommandObject> commandQueue = new LinkedBlockingQueue<>();

	private final CLIPSInterpreter interpreter;

	private int idCounter = 0;

	private final Thread commandThread;

	/** A parsed command waiting to be executed, and the channel it came from. */
	public record CommandObject(Object command, String channelId) {
	}

	public MessageRouter(Rete engine) {
		this.engine = engine;
		this.interpreter = new CLIPSInterpreter(engine);
		this.commandThread = Thread.ofPlatform().name("morendo-router").daemon(true).unstarted(this::runCommands);
		this.commandThread.start();
	}

	private void runCommands() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				CommandObject next = commandQueue.take();
				currentChannelId = next.channelId();
				try {
					postMessageEvent(new MessageEvent(MessageEvent.Type.COMMAND, next.command(), currentChannelId));
					ReturnVector result = interpreter.executeCommand(next.command());
					postMessageEvent(new MessageEvent(MessageEvent.Type.RESULT, result, currentChannelId));
				} catch (Exception e) {
					postMessageEvent(new MessageEvent(MessageEvent.Type.ERROR, e, currentChannelId));
				} finally {
					currentChannelId = null;
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/** Stops the command thread. Called when the engine is closed. */
	public void shutdown() {
		commandThread.interrupt();
	}

	public Rete getReteEngine() {
		return engine;
	}

	/** Delivers an event to every channel interested in it. May be called from any thread. */
	public void postMessageEvent(MessageEvent event) {
		synchronized (idToChannel) {
			for (CommunicationChannel channel : idToChannel.values()) {
				if (InterestType.ALL.equals(channel.getInterest()) || (InterestType.MINE.equals(channel.getInterest())
						&& channel.getChannelId().equals(event.getChannelId()))) {
					List<MessageEvent> messageList = idToMessages.get(channel.getChannelId());
					if (messageList != null) {
						messageList.add(event);
					}
				}
			}
		}
	}

	public void enqueueCommand(Object command, String channelId) {
		commandQueue.add(new CommandObject(command, channelId));
	}

	public CommandObject dequeueCommand() {
		return commandQueue.poll();
	}
	public StreamChannel openChannel(String channelName, InputStream inputStream) {
		return openChannel(channelName, inputStream, InterestType.MINE);
	}

	public StreamChannel openChannel(String channelName,
			InputStream inputStream, InterestType interestType) {
		StreamChannel channel = new StreamChannelImpl(channelName + "_"
				+ idCounter++, this, interestType);
		channel.init(inputStream);
		registerChannel(channel);
		return channel;
	}

	public StreamChannel openChannel(String channelName, Reader reader) {
		return openChannel(channelName, reader, InterestType.MINE);
	}

	public StreamChannel openChannel(String channelName, Reader reader,
			InterestType interestType) {
		StreamChannel channel = new StreamChannelImpl(channelName + "_"
				+ idCounter++, this, interestType);
		channel.init(reader);
		registerChannel(channel);
		return channel;
	}

	public StringChannel openChannel(String channelName) {
		return openChannel(channelName, InterestType.MINE);
	}

	public StringChannel openChannel(String channelName,
			InterestType interestType) {
		StringChannel channel = new StringChannelImpl(channelName + "_"
				+ idCounter++, this, interestType);
		registerChannel(channel);
		return channel;
	}

	public void closeChannel(CommunicationChannel channel) {
		synchronized (idToChannel) {
			idToChannel.remove(channel.getChannelId());
			idToMessages.remove(channel.getChannelId());
			// If it's a StreamChannel, stop the Parser-Thread
			if(channel instanceof StreamChannelImpl) {
				((StreamChannelImpl)channel).close();
			}
		}
	}

	public void closeChannel(String channelName) {
		CommunicationChannel channel  = idToChannel.get(channelName);
		if(channel instanceof StreamChannelImpl) {
			((StreamChannelImpl)channel).close();
		}
	}
	
	private void registerChannel(CommunicationChannel channel) {
		synchronized (idToChannel) {
			idToChannel.put(channel.getChannelId(), channel);
			idToMessages.put(channel.getChannelId(), new ArrayList<MessageEvent>());
		}
	}

	void fillMessageList(String channelId, List<MessageEvent> destinationList) {
		synchronized (idToChannel) {
			List<MessageEvent> storedMessages = idToMessages.get(channelId);
			if (storedMessages != null && destinationList != null) {
				destinationList.addAll(storedMessages);
				storedMessages.clear();
			}
		}
	}

	public void setCurrentChannelId(String id) {
		this.currentChannelId = id;
	}
	
	/** The channel whose command is executing, or the first registered channel when none is. */
	public String getCurrentChannelId() {
		String id = this.currentChannelId;
		if (id == null) {
			synchronized (idToChannel) {
				id = idToMessages.isEmpty() ? "" : idToMessages.keySet().iterator().next();
			}
			this.currentChannelId = id;
		}
		return id;
	}
}

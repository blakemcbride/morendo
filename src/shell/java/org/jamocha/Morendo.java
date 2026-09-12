package org.jamocha;

import javax.swing.SwingUtilities;

import org.jamocha.gui.JamochaGui;
import org.jamocha.rete.Rete;
import org.jamocha.shell.Shell;

/**
 * Command-line entry point: "-shell" runs the interactive shell on the calling thread,
 * "-gui" opens the Swing GUI on the event dispatch thread; both may be given.
 */
public class Morendo {

	private JamochaGui jamochaGui;

	private Shell shell;

	private final Rete engine;

	public static void main(String[] args) {
		boolean guiStarted = false;
		boolean shellStarted = false;
		Morendo morendo = new Morendo(new Rete());
		if (args != null) {
			for (String arg : args) {
				if (arg.equalsIgnoreCase("-gui")) {
					morendo.startGui();
					guiStarted = true;
				} else if (arg.equalsIgnoreCase("-shell")) {
					shellStarted = true;
				}
			}
		}
		if (!shellStarted && !guiStarted) {
			morendo.showUsage();
			return;
		}
		if (guiStarted && !shellStarted) {
			morendo.getJamochaGui().setExitOnClose(true);
		}
		if (shellStarted) {
			morendo.runShell();
		}
	}

	Morendo(Rete engine) {
		this.engine = engine;
	}

	/** Runs the shell on the calling thread until the input ends or the engine is closed. */
	public void runShell() {
		if (shell == null) {
			shell = new Shell(engine);
			shell.run();
		}
	}

	public void startGui() {
		if (jamochaGui == null) {
			jamochaGui = new JamochaGui(engine);
			SwingUtilities.invokeLater(jamochaGui::showGui);
		}
	}

	public void showUsage() {
		String sep = System.lineSeparator();
		System.out.println("You have to pass one or more of the following arguments:" + sep + sep
				+ "-gui:   starts a graphical user interface." + sep
				+ "-shell: starts a simple Shell.");
	}

	public JamochaGui getJamochaGui() {
		return jamochaGui;
	}

	public Shell getShell() {
		return shell;
	}
}

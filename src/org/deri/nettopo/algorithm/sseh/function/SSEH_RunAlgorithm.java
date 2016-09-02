package org.deri.nettopo.algorithm.sseh.function;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.deri.nettopo.algorithm.AlgorFunc;
import org.deri.nettopo.algorithm.Algorithm;
import org.deri.nettopo.algorithm.cknplus.function.CKNplus_MAIN;
import org.deri.nettopo.algorithm.tpgf.function.TPGF_ConnectNeighbors;
import org.deri.nettopo.app.NetTopoApp;

public class SSEH_RunAlgorithm implements AlgorFunc{
	private Algorithm algorithm;
	private SSEH_MAIN sseh;
	private SSEH_ConnectNeighbors connectNeighbors;

	public SSEH_RunAlgorithm(Algorithm algorithm)
	{
		this.algorithm = algorithm;
		sseh = new SSEH_MAIN();
		connectNeighbors = new SSEH_ConnectNeighbors();
	}

	public SSEH_RunAlgorithm()
	{
		this(null);
	}

	@Override
	public void run()
	{
		NetTopoApp app = NetTopoApp.getApp();
		Timer timer = app.getTimer_func();
		TimerTask task = app.getTimertask_func();
		if (timer != null && task != null)
		{
			task.cancel();
			timer.cancel();
			timer.purge();
			app.setTimertask_func(null);
			app.setTime_func(null);
		}

		app.setTime_func(new Timer());
		app.setTimertask_func(new TimerTask()
		{
			@Override
			public void run()
			{
				entry();
			}
		});
		app.getTimer_func().schedule(app.getTimertask_func(), 0,
				app.getFunc_INTERVAL() * 1000);
	}

	public void entry()
	{
		sseh.run();
		connectNeighbors.run();
		final StringBuffer message = new StringBuffer();
		int[] activeSensorNodes = NetTopoApp.getApp().getNetwork()
				.getSensorActiveNodes();
		message.append("k=" + sseh.getK() + ", Number of active nodes is:"
				+ activeSensorNodes.length + ", they are: "
				+ Arrays.toString(activeSensorNodes));

		NetTopoApp.getApp().getDisplay().asyncExec(new Runnable()
		{
			@Override
			public void run()
			{
				NetTopoApp.getApp().refresh();
				NetTopoApp.getApp().addLog(message.toString());
			}
		});
	}

	public Algorithm getAlgorithm()
	{
		return algorithm;
	}


}

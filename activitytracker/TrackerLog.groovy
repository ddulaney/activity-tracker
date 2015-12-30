package activitytracker
import com.intellij.concurrency.JobScheduler
import com.intellij.openapi.Disposable
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.annotations.Nullable

import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentLinkedQueue

import static java.util.concurrent.TimeUnit.MILLISECONDS
import static liveplugin.implementation.Misc.newDisposable

class TrackerLog {
	private final static long writeFrequencyMs = 1000
	private final String statsFilePath
	private final Disposable parentDisposable
	private final Queue<TrackerEvent> eventQueue = new ConcurrentLinkedQueue<>()

	TrackerLog(String path, Disposable parentDisposable) {
		def pathFile = new File(path)
		if (!pathFile.exists()) pathFile.mkdir()
		this.statsFilePath = path + "/stats.csv"
		this.parentDisposable = parentDisposable
	}

	def init() {
		def runnable = {
			def file = new File(statsFilePath)
			def event = eventQueue.poll()
			while (event != null) {
				file.append(event.toCsv() + "\n")
				event = eventQueue.poll()
			}
		} as Runnable
		def future = JobScheduler.scheduler.scheduleAtFixedRate(runnable, writeFrequencyMs, writeFrequencyMs, MILLISECONDS)
		newDisposable(parentDisposable) {
			future.cancel(true)
		}
		this
	}

	def append(@Nullable TrackerEvent event) {
		if (event == null) return
		eventQueue.add(event)
	}

	def resetHistory() {
		new File(statsFilePath).delete()
	}

	List<TrackerEvent> readHistory(Date fromTime, Date toTime) {
		new File(statsFilePath).withReader { reader ->
			def result = []
			String line
			while ((line = reader.readLine()) != null) {
				def event = TrackerEvent.fromCsv(line)
				if (event.time.after(fromTime) && event.time.before(toTime))
					result << event
				if (event.time.after(toTime)) break
			}
			result
		}
	}

	void rollFile() {
		def postfix = new SimpleDateFormat("_yyyyMMdd_HHmmss").format(new Date())
		def statsFile = new File(statsFilePath)
		FileUtil.rename(statsFile, new File(statsFilePath + postfix))
	}

	File currentLogFile() {
		new File(statsFilePath)
	}
}
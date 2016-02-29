package net.sf.ecl1.git;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jgit.api.Git;

/**
 * Executes a pull command on all open projects using Git as SCM
 *  
 * @author keunecke
 */
public class GitBatchPullHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Job job = new Job("ecl1GitBatchPull") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				List<IProject> projects = Arrays.asList(ResourcesPlugin.getWorkspace().getRoot().getProjects());
				monitor.beginTask("Batch Git Pull", projects.size());
				for (IProject p : projects) {
					monitor.subTask("Pulling " + p.getName());
					File projectLocationFile = p.getLocation().toFile();
					try {
						Git git = Git.open(projectLocationFile);
						Set<String> remotes = git.getRepository().getRemoteNames();
						if(remotes != null && !remotes.isEmpty()) {
							git.pull();
						}
					} catch (org.eclipse.jgit.errors.RepositoryNotFoundException rnfe) {
						// ignore
					} catch (IOException e) {
						e.printStackTrace();
					}
					monitor.worked(1);
				}
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return null;
	}

}

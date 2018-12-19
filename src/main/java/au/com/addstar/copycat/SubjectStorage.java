package au.com.addstar.copycat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.google.common.collect.HashMultimap;

public class SubjectStorage
{
	private File mFolder;
	
	private HashMultimap<Integer, Subject> mLoadedSubjects;
	
	public SubjectStorage(File folder)
	{
		mFolder = folder;
		if(!mFolder.exists())
			mFolder.mkdirs();
		
		mLoadedSubjects = HashMultimap.create();
	}
	
	public boolean add(Subject subject)
	{
		File subjectFolder = new File(mFolder, String.valueOf(subject.getSize()));
		if(!subjectFolder.exists())
			subjectFolder.mkdirs();
		
		File dest = (subject.getFile() == null ? new File(subjectFolder, UUID.randomUUID().toString() + ".yml") : subject.getFile());
		
		try
		{
			subject.save(dest);
			
			if(!mLoadedSubjects.containsKey(subject.getSize()))
				loadSubjects(subject.getSize());
			mLoadedSubjects.put(subject.getSize(), subject);
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean remove(Subject subject)
	{
		if(!mLoadedSubjects.remove(subject.getSize(), subject))
			return false;
		
		if(subject.getFile() == null)
			return true; // Nothing to delete
		
		return subject.getFile().delete();
	}
	
	public void loadSubjects(int size)
	{
		File subjectFolder = new File(mFolder, String.valueOf(size));
		if(!subjectFolder.exists())
			return;
		
		mLoadedSubjects.removeAll(size);
		for(File file : subjectFolder.listFiles())
		{
			if(file.isFile() && file.getName().toLowerCase().endsWith(".yml"))
			{
				try
				{
					Subject subject = Subject.from(file);
					mLoadedSubjects.put(size, subject);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public Subject getRandomSubject(int size)
	{
		if(!mLoadedSubjects.containsKey(size))
			loadSubjects(size);
		
		ArrayList<Subject> subjects = new ArrayList<>(mLoadedSubjects.get(size));
		if(subjects.isEmpty())
			return null;
		
		return subjects.get(CopyCatPlugin.rand.nextInt(subjects.size()));
	}
}

# About

This is Quick-Setup. It can be used to make template projects that can be used later. The idea is to get rid of repeated steps when creating new projects.
Currently I am working on some code that can be used to replace text in the template code, so the template code automatically get's renamed.

# How to setup

The first thing you need to do is to setup your environment variables:

In home directory:
```bash
sudo nano /etc/environment
```
write
```python
qspath=/path/to/Setup/
sudo nano .bashrc
```
At the end of .bashrc
```python
export PATH = "$qspath:$PATH"
```
Now that you've added your environment variables and added it to path. You can now setup the jar files
In the code directory:
```bash
sudo apt install default-jre
sudo apt-get install binfmt-support
sudo apt-get install jarwrapper
chmod u+x qs.jar
```
This makes it so that you can use the jar files by simply typing qs.jar in the terminal.

# How to use
## Commands
 Your first option is to use init
 ```bash
qs.jar init <name of template>
qs.jar init
qs.jar init all
```
All of these commands add a template to path.qs. 
Not entering anything after init will result in a template named after the folder
Init all init's every folder in the current directory, and makes a template out of them

Then you can actually add the files to the current working directory by writing
```bash
qs.jar <name of template>
--> qs.jar <name of template> <type>
qs.jar <input directory>
--> qs.jar <input directory> <type>
```
You can input the name of a template if you want to add the template files to a directory
If you don't input a valid template name it will assume you're inputing in a directory and it will copy all the files from that directory

## Conf files
For now the only feature is the type modifier.
The type modifier is written as so

    type=<type number>
The types that you need to know are
```c
1 - copys all files from one directory to another
2 - deletes all files and they copys them
3 - replaces all files when copying them
```

you can make a .qsconf file in each template folder. The file must be called .qsconf and nothing else
You can also change default.qsconf; changing this means that if a don't specify a type in your command it will default to the type you set here

The priority of types goes as such

    command
    template.qsconf
    default.qsconf

When one isn't defined the next highest piority is picked.
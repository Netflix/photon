#!/usr/bin/env bash

# This script:
#  - Is intended to quickly get people up and running using Photon
#  - Builds and installs Photon on Mac & Linux
#  - Generates an IMP validator script in /usr/local/bin/photon_imp_validator.sh
#  - Can be run using \curl -sSL https://raw.githubusercontent.com/Netflix/photon/master/bootstrap.sh | bash


photon_git_repo_url=https://github.com/Netflix/photon.git
photon_git_repo_parent_dir=~/Projects/stash
photon_git_repo_dir=${photon_git_repo_parent_dir}/photon
photon_install_dir=~/dev/photon_builds/$(date +"%Y-%m-%d")
photon_validator_script_to_generate=${photon_install_dir}/photon_imp_validator.sh
photon_validator_script_install_dir=/usr/local/bin
photon_validator_script_installed_path=${photon_validator_script_install_dir}/photon_imp_validator.sh


die() { echo "$@" 1>&2 ; exit 1; }


echo "Checking whether git is installed.."
git --version 2>&1 >/dev/null
GIT_IS_AVAILABLE=$?
if [ ! $GIT_IS_AVAILABLE -eq 0 ]; then 
	die "Git is not installed, please install it. On Mac you can type 'brew install git', on Ubuntu: 'sudo apt-get install git', on CentOS/Redhat: sudo yum install git" 
else 
	echo "Git found"
fi


echo "Checking for JDK / Java compiler.."
if type -p javac; then
    echo "Found Java compiler in $(which javac)"
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/javac" ]];  then
    echo "Found Java compiler in ${JAVA_HOME}/bin/javac"
else
    die "Error: JDK not found, please install the latest version and make sure the JAVA_HOME environment variable is set correctly."
fi


if [ ! -e ${photon_git_repo_dir} ] ; then
	echo "Git repo does not exist here, creating it: ${photon_git_repo_dir}"
	mkdir -p $photon_git_repo_dir || die "Couldn't create git repo dir"
	cd $photon_git_repo_parent_dir
	echo "Cloning git repo: git clone ${photon_git_repo_url}"
	git clone ${photon_git_repo_url} || die "Error cloning git repo"
fi


echo "Building Photon.."
echo "Changing to build dir: cd ${photon_git_repo_dir}"
cd ${photon_git_repo_dir} || die "Error: can't cd to ${photon_git_repo_dir}"

echo "Fetching latest code.."
git fetch || die "Error running git fetch"

echo "Determining the latest release.."
latest_release=$(curl -s https://api.github.com/repos/netflix/photon/releases/latest | grep 'tag_name' | grep -o 'v.*"' | tr -d '"')
echo "Latest release found: ${latest_release}"
check_this_out=master

if [[ ! -z "${latest_release}"  ]] ; then
	check_this_out=${latest_release}
fi

echo "Checking out ${check_this_out}"
git checkout ${check_this_out} || die "Error: can't checkout ${check_this_out} in ${photon_git_repo_dir}"

echo "Cleaning the build dir"
./gradlew clean || die "Failure while running ./gradlew clean"
echo "Building Photon"
./gradlew build || die "Failure while running ./gradlew build"
echo "Getting dependencies"
./gradlew getDependencies || die "Failure while running ./gradlew getDependencies"

if [ -e ${photon_install_dir} ] ; then
	echo "Photon install dir already exists, removing: rm -rf ${photon_install_dir}"
	rm -rf ${photon_install_dir}
fi


echo "Creating a dir to install the java classes: ${photon_install_dir}"
mkdir -p ${photon_install_dir} || die "Error running mkdir -p ${photon_install_dir}"
echo "Installing java classes"
cp -r ./build/libs ${photon_install_dir}/ || die "Error running cp -r ./build/libs ${photon_install_dir}/"



echo "Creating IMP validation script: ${photon_validator_script_to_generate}"
cat <<EOT > ${photon_validator_script_to_generate}
#!/usr/bin/env bash

set -x 

java -cp ${photon_install_dir}/libs/*:* com.netflix.imflibrary.app.PhotonIMPAnalyzer \${1} > \${1}_IMP_validation_output.txt

EOT


echo "Installing photon validator script: cp ${photon_validator_script_to_generate} ${photon_validator_script_install_dir}/"
cp ${photon_validator_script_to_generate} ${photon_validator_script_install_dir}/

if [ ! -e  ${photon_validator_script_installed_path} ] ; then
	echo "Script didn't install, using sudo"
	sudo cp ${photon_validator_script_to_generate} ${photon_validator_script_install_dir}/
	echo "Setting permissions: sudo chmod ugo+x ${photon_validator_script_installed_path}"
	sudo chmod ugo+x ${photon_validator_script_installed_path} || die "Error setting permissions on IMP validator script: chmod ugo+x ${photon_validator_script_installed_path} "
else
	echo "Setting permissions: chmod ugo+x ${photon_validator_script_installed_path}"
	chmod ugo+x ${photon_validator_script_installed_path} || die "Error setting permissions on IMP validator script: chmod ugo+x ${photon_validator_script_installed_path} "
fi



echo "Done"
cat <<EOT

To run IMP validation:
	photon_imp_validator.sh </dir/containing/imp|/path/to/assetmap.xml>

	A validation report will be created named: </path/to/imp>_IMP_validation_output.txt


EOT

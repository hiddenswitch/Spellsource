import git
from typing import List
import os.path as path
import os
import shutil
import json
from glob import glob


class PullRequestFixSession(object):
    repo: git.Repo
    diff: List[git.Diff]

    def __init__(self, repo_path: str):
        self.repo = git.Repo(repo_path)

    def fix(self) -> List[str]:
        res = []
        self.diff = self.repo.tree().diff()
        for diff in self.diff:
            if 'cards/src/main' not in diff.a_path:
                continue
            if diff.deleted_file:
                # undelete files, mark them as not collectible instead
                filepath = diff.a_path
                self.repo.git.checkout('HEAD', '--', filepath)
                with open(filepath, 'r') as fp:
                    contents = json.load(fp)
                contents['collectible'] = False
                with open(filepath, 'w') as fp:
                    json.dump(contents, fp)
                res += [filepath]
            if diff.renamed_file:
                # check that the file name has been changed
                if path.basename(diff.rename_to) == path.basename(diff.rename_from):
                    # No fix necessary
                    continue
                # continue with fix
                newpath = path.basename(diff.rename_from)
                # check that the directory exists
                destination = path.join(path.dirname(diff.rename_from), newpath)
                os.makedirs(path.dirname(destination), exist_ok=True)
                shutil.move(diff.rename_to, destination)
                res += [newpath]
        # fix all missing .json extensions and double .json extensions
        for filepath in glob('cards/src/main/resources/cards/**', recursive=True):
            if not path.isfile(filepath):
                continue
            if not filepath.endswith('.json'):
                json_ = filepath + '.json'
                shutil.move(filepath, json_)
                res += [json_]
            elif filepath.endswith('.json.json'):
                # retrieve the real path name
                basename, ext = path.splitext(filepath)
                while ext != '':
                    basename, ext = path.splitext(basename)
                basename__json_ = basename + '.json'
                shutil.move(filepath, basename__json_)
                res += [basename__json_]
        return res

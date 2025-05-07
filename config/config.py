

from os import environ, getenv, path

from jinja2 import Environment, FileSystemLoader

templates_folder = path.join(path.dirname(__file__), "templates")
templateLoader = FileSystemLoader(searchpath=templates_folder)

def env_override(value, key):
    return getenv(key, value)

env = Environment(loader=templateLoader)

env.filters["env"] = env_override
TEMPLATE_FILE_NAME = "job.ini.j2"

template = env.get_template(TEMPLATE_FILE_NAME)
conf = template.render(env=environ)

with open(path.join(path.dirname(__file__), "job.ini"), "w", encoding="utf-8") as f:
    f.write(conf)
